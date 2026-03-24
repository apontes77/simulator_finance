import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.example.ExtraPayment
import org.example.LoanParameters
import org.example.calculateSAC
import org.example.percent
import org.example.reais
import java.math.BigDecimal

class SacCalculationTest : StringSpec({
    "O saldo devedor deve ser zero ao final do prazo na Tabela SAC" {
        //arrange
        val params = LoanParameters(
        totalAmount = 100000.0.reais,
        yearlyInterestRate = 12.0.percent,
        termInMonths = 12
        )

        //act
        val schedule = calculateSAC(params)

        //assert
        val lastInstallment = schedule.last()
        lastInstallment.remainingBalance.value shouldBe BigDecimal("0.04")
        schedule.size shouldBe 12
    }

    "Um aporte extra deve reduzir o prazo total do financiamento" {
        val params = LoanParameters(
            totalAmount = 100000.0.reais,
            yearlyInterestRate = 10.0.percent,
            termInMonths = 120
        )

        val extras = listOf(ExtraPayment(2, 50000.0.reais))

        val scheduleBase = calculateSAC(params)
        val scheduleWithExtra = calculateSAC(params, extras)

        scheduleWithExtra.size shouldBeLessThan scheduleBase.size
        println("redução de ${scheduleBase.size - scheduleWithExtra.size} meses com o aporte")
    }

    "A amortização (principal) deve ser constante em todos os meses na SAC (sem extras)" {
        val params = LoanParameters(
            totalAmount = 120000.0.reais,
            yearlyInterestRate = 10.0.percent,
            termInMonths = 12
        )

        val schedule = calculateSAC(params)

        // Na SAC de 120k em 12 meses, cada mês deve amortizar exatamente 10k
        schedule.forEach { installment ->
            installment.principal.value shouldBe BigDecimal("10000.00")
        }
    }
})