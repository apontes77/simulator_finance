package org.example


import java.math.BigDecimal
import java.math.RoundingMode

// 1. O Motor de Cálculo (Função Pura)
fun calculateSAC(params: LoanParameters): List<Installment> {
    val installments = mutableListOf<Installment>()
    var currentBalance = params.totalAmount

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
        val interest = currentBalance * monthlyRate.toDouble()
        val totalInstallment = monthlyPrincipal + interest

        // Abate do saldo devedor
        currentBalance -= monthlyPrincipal

        // Se for o último mês, ajustamos resíduos de arredondamento
        val adjustedBalance = if (month == params.termInMonths) 0.0.reais else currentBalance

        installments.add(
            Installment(
                number = month,
                principal = monthlyPrincipal,
                interest = interest,
                total = totalInstallment,
                remainingBalance = adjustedBalance
            )
        )

        currentBalance = adjustedBalance
    }

    return installments
}

// 2. O Main para testar a lógica
fun main() {
    val myLoan = LoanParameters(
        totalAmount = 200000.0.reais,
        yearlyInterestRate = 12.0.percent, // 1% ao mês aprox.
        termInMonths = 12 // Simulação curta para testar
    )

    val schedule = calculateSAC(myLoan)

    println("--- SIMULAÇÃO DE FINANCIAMENTO (SAC) ---")
    println("Valor: ${myLoan.totalAmount} | Taxa: ${myLoan.yearlyInterestRate.value}%")
    println("----------------------------------------")

    schedule.take(5).forEach { i ->
        println("Mês ${i.number}: Parcela ${i.total} (Amort: ${i.principal} | Juros: ${i.interest}) | Saldo: ${i.remainingBalance}")
    }

    val totalJuros = schedule.sumOf { it.interest.value }
    println("----------------------------------------")
    println("Total de Juros Pagos: R$ $totalJuros")
}

// Helper para somar BigDecimals em coleções Kotlin
fun Iterable<Installment>.sumOf(selector: (Installment) -> BigDecimal): BigDecimal =
    this.map(selector).fold(BigDecimal.ZERO, BigDecimal::add)