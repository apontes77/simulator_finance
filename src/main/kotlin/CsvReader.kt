package org.example

import java.io.File

fun loadExtraPaymentFromCsv(filePath: String): List<ExtraPayment> {
    val file = File(filePath)

    if (!file.exists()) {
        println("Aviso: Arquivo $filePath não encontrado.")
        return emptyList()
    }

    return file.readLines()
        .drop(1) //pula o cabecalho (mes, valor)
        .filter { it.isNotBlank() } //ignora linhas vazias
        .mapNotNull { line ->
            try {
                val columns = line.split(",")
                val month = columns[0].trim().toInt()
                val amount = columns[1].trim().toDouble().reais
                ExtraPayment(month, amount)
            } catch (e: Exception) {
                println("erro ao processar linha: '$line'.")
                null //o mapNotNull vai descartar essa linha em vez de quebrar o app
            }
        }
}

//fun main () {
//    val params = LoanParameters(
//        totalAmount = 250000.0.reais,
//        yearlyInterestRate = 11.5.percent,
//        termInMonths = 360
//    )
//
//    //lendo do arquivo csv
//    val csvPath = "amortizacoes.csv"
//    val extraPayments = loadExtraPaymentFromCsv(csvPath)
//
//    val baseSchedule = calculateSAC(params)
//    val optimizedSchedule = calculateSAC(params, extraPayments)
//
//    printComparison(
//        base = baseSchedule.toSummary("cenário base", params.startDate),
//        optimized = optimizedSchedule.toSummary("cenário com csv", params.startDate)
//    )
//}



suspend fun main() {
    val bcbService = BcbService()

    println("🔍 Consultando Selic atual no Banco Central...")
    val currentSelic = bcbService.fetchCurrentSelicRate()
    println("✅ Taxa Selic encontrada: $currentSelic% ao ano.\n")

    val params = LoanParameters(
        totalAmount = 250000.0.reais,
        yearlyInterestRate = currentSelic.percent, // Usando a taxa real da API
        termInMonths = 360
    )

    val csvPath = "amortizacoes.csv"
    val extraPayments = loadExtraPaymentFromCsv(csvPath)

    val baseSchedule = calculateSAC(params)
    val optimizedSchedule = calculateSAC(params, extraPayments)

    printComparison(
        base = baseSchedule.toSummary("Cenário Base (Selic)", params.startDate),
        optimized = optimizedSchedule.toSummary("Cenário com Aportes", params.startDate)
    )
}