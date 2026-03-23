package org.example


import java.math.BigDecimal
import java.math.RoundingMode

// 1. O Motor de Cálculo (Função Pura)
fun calculateSAC(params: LoanParameters, extraPayments: List<ExtraPayment> = emptyList()): List<Installment> {
    val installments = mutableListOf<Installment>()
    var currentBalance = params.totalAmount

    // mapeamos os aports pelo mes para busca rapida
    val extrasMap = extraPayments.associateBy { it.month }

    val monthlyPrincipalBase = Money(
        params.totalAmount.value.divide(
            params.termInMonths.toBigDecimal(),
            2,
            RoundingMode.HALF_UP
        )
    )

    // Na SAC, a amortização (principal) é constante: Valor Total / Meses
    val monthlyPrincipal = Money(
        params.totalAmount.value.divide(
            params.termInMonths.toBigDecimal(),
            2,
            RoundingMode.HALF_UP
        )
    )

    val monthlyRate = params.yearlyInterestRate.toDecimal()
        .divide(BigDecimal("12"), 8, RoundingMode.HALF_UP)

    for (month in 1..params.termInMonths) {
        if (currentBalance <= 0.0.reais) break

        val interest = currentBalance * monthlyRate.toDouble()

        // Verifica se há aporte extra para este mês
        val extra = extrasMap[month]?.amount ?: 0.0.reais

        // Na SAC, a amortização mensal é o Principal Base + Aporte Extra
        // Mas não podemos amortizar mais do que o saldo devedor atual
        val actualPrincipal = if (monthlyPrincipalBase + extra > currentBalance) {
            currentBalance
        } else {
            monthlyPrincipalBase + extra
        }

        val totalInstallment = actualPrincipal + interest
        currentBalance -= actualPrincipal

        installments.add(
            Installment(
                number = month,
                principal = actualPrincipal,
                interest = interest,
                total = totalInstallment,
                remainingBalance = currentBalance
            )
        )
    }

    return installments
}



// 2. O Main para testar a lógica
//fun main() {
//    val myLoan = LoanParameters(
//        totalAmount = 200000.0.reais,
//        yearlyInterestRate = 12.0.percent, // 1% ao mês aprox.
//        termInMonths = 12 // Simulação curta para testar
//    )
//
//    val schedule = calculateSAC(myLoan)
//
//    println("--- SIMULAÇÃO DE FINANCIAMENTO (SAC) ---")
//    println("Valor: ${myLoan.totalAmount} | Taxa: ${myLoan.yearlyInterestRate.value}%")
//    println("----------------------------------------")
//
//    schedule.take(5).forEach { i ->
//        println("Mês ${i.number}: Parcela ${i.total} (Amort: ${i.principal} | Juros: ${i.interest}) | Saldo: ${i.remainingBalance}")
//    }
//
//    val totalJuros = schedule.sumOf { it.interest.value }
//    println("----------------------------------------")
//    println("Total de Juros Pagos: R$ $totalJuros")
//}

fun main() {
    val myLoan = LoanParameters(
        totalAmount = 200000.0.reais,
        yearlyInterestRate = 10.0.percent,
        termInMonths = 120
    )

    // Definindo aportes extras
    val extras = listOf(
        ExtraPayment(12, 20000.0.reais), // Bônus anual
        ExtraPayment(24, 10000.0.reais),
        ExtraPayment(36, 10000.0.reais)
    )

    val schedule = calculateSAC(myLoan, extras)

    println("--- SIMULAÇÃO COM AMORTIZAÇÃO EXTRA ---")
    println("Prazo Original: ${myLoan.termInMonths} meses")
    println("Prazo Realizado: ${schedule.size} meses") // Aqui vemos a mágica

    val economiaMeses = myLoan.termInMonths - schedule.size
    println("Economia de Tempo: $economiaMeses meses (aprox. ${economiaMeses / 12} anos)")

    val totalJurosComExtra = schedule.sumOf { it.interest.value }

    // Simulação sem extras para comparar
    val scheduleSemExtra = calculateSAC(myLoan)
    val totalJurosSemExtra = scheduleSemExtra.sumOf { it.interest.value }

    val economiaFinanceira = totalJurosSemExtra - totalJurosComExtra
    println("Economia de Juros: R$ $economiaFinanceira")

    println("\n--- DETALHE DO MÊS DO APORTE (Mês 12) ---")
    schedule.find { it.number == 12 }?.let {
        println("Parcela: ${it.total} | Amortização Total: ${it.principal}")
    }
}

// Helper para somar BigDecimals em coleções Kotlin
fun Iterable<Installment>.sumOf(selector: (Installment) -> BigDecimal): BigDecimal =
    this.map(selector).fold(BigDecimal.ZERO, BigDecimal::add)