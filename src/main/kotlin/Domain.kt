package org.example

import java.math.BigDecimal
import java.math.RoundingMode

// 1. Value Class para Moeda - Proteção de tipo e performance
@JvmInline
value class Money(val value: BigDecimal) : Comparable<Money> {

    //implementa a funcao compareTo
    override fun compareTo(other: Money): Int = this.value.compareTo(other.value)

    operator fun plus(other: Money) = Money(this.value.add(other.value).setScale(2, RoundingMode.HALF_UP))
    operator fun minus(other: Money) = Money(this.value.subtract(other.value).setScale(2, RoundingMode.HALF_UP))
    operator fun times(factor: Double) = Money(this.value.multiply(factor.toBigDecimal()).setScale(2, RoundingMode.HALF_UP))

    override fun toString() = "R$ ${value.setScale(2, RoundingMode.HALF_UP)}"
}

// 2. Value Class para Taxa de Juros - Evita confundir Taxa com Dinheiro
@JvmInline
value class InterestRate(val value: BigDecimal) {
    fun toDecimal() = value.divide(BigDecimal("100"), 8, RoundingMode.HALF_UP)
}

// 3. DSL Extensions - Para deixar o código "limpo" e legível
val Double.reais get() = Money(this.toBigDecimal())
val Double.percent get() = InterestRate(this.toBigDecimal())

// 4. Data Classes para os Cenários
data class LoanParameters(
    val totalAmount: Money,
    val yearlyInterestRate: InterestRate,
    val termInMonths: Int,
    val startDate: java.time.LocalDate = java.time.LocalDate.now()
)

data class Installment(
    val number: Int,
    val principal: Money,  // Amortização (o que abate da dívida)
    val interest: Money,   // Juros do mês
    val total: Money,      // Parcela total (Amortização + Juros)
    val remainingBalance: Money // Saldo Devedor após a parcela
)

data class ExtraPayment(
    val month: Int,
    val amount: Money
)