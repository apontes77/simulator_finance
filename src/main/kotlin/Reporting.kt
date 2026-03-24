package org.example

data class SimulationSummary(
    val description: String,
    val totalInterest: Money,
    val totalPaid: Money,
    val durationMonths: Int,
    val lastPaymentDate: java.time.LocalDate
)

// Função de conveniência para transformar a lista de parcelas em um resumo
fun List<Installment>.toSummary(description: String, startDate: java.time.LocalDate): SimulationSummary {
    val interest = Money(this.sumOf { it.interest.value })
    val principal = Money(this.sumOf { it.principal.value })
    return SimulationSummary(
        description = description,
        totalInterest = interest,
        totalPaid = interest + principal,
        durationMonths = this.size,
        lastPaymentDate = startDate.plusMonths(this.size.toLong())
    )
}

fun printComparison(base: SimulationSummary, optimized: SimulationSummary) {
    val interestSaved = base.totalInterest - optimized.totalInterest
    val monthsSaved = base.durationMonths - optimized.durationMonths

    println("\n" + "=".repeat(50))
    println("📊 RELATÓRIO COMPARATIVO DE FINANCIAMENTO")
    println("=".repeat(50))

    val header = "%-20s | %-12s | %-12s".format("Métrica", "Base", "Otimizado")
    println(header)
    println("-".repeat(50))

    println("%-20s | %-12s | %-12s".format("Total de Juros", base.totalInterest, optimized.totalInterest))
    println("%-20s | %-12s | %-12s".format("Custo Total", base.totalPaid, optimized.totalPaid))
    println("%-20s | %-12s | %-12s".format("Duração (meses)", base.durationMonths, optimized.durationMonths))
    println("%-20s | %-12s | %-12s".format("Data de Quitação", base.lastPaymentDate, optimized.lastPaymentDate))

    println("-".repeat(50))
    println("💡 IMPACTO DA ESTRATÉGIA:")
    println("✅ Economia de Juros: $interestSaved")
    println("✅ Tempo reduzido em: $monthsSaved meses (${monthsSaved / 12} anos e ${monthsSaved % 12} meses)")
    println("=".repeat(50) + "\n")
}

fun main() {
    val params = LoanParameters(
        totalAmount = 250000.0.reais,
        yearlyInterestRate = 11.5.percent,
        termInMonths = 360 // 30 anos (padrão Brasil)
    )

    // Cenário 1: Sem aportes
    val baseSchedule = calculateSAC(params)
    val baseSummary = baseSchedule.toSummary("Cenário Base", params.startDate)

    // Cenário 2: Amortização agressiva (R$ 1.500 extras todo mês)
    // Usamos o range 1..360 para injetar aportes mensais
    val extraPayments = (1..360).map { month ->
        ExtraPayment(month, 1500.0.reais)
    }

    val optimizedSchedule = calculateSAC(params, extraPayments)
    val optimizedSummary = optimizedSchedule.toSummary("Cenário Otimizado", params.startDate)

    // Gerar Relatório
    printComparison(baseSummary, optimizedSummary)
}