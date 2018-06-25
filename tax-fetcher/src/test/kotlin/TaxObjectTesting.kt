import weesner.tax_fetcher.*
import weesner.tax_fetcher.FederalIncomeTax.Companion.withholding
import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.ficaTaxableAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.payPeriodType
import weesner.tax_fetcher.FederalTaxModel.Companion.payrollAllowances
import weesner.tax_fetcher.FederalTaxModel.Companion.yearToDateGross
import kotlin.test.Test

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/27/2018
 */
class TaxObjectTesting {
    @Test
    fun testNewGson() {
        val taxModel = getFederalTaxes()
        taxModel.apply {
            yearToDateGross = 2000000.0
            checkAmount = 400.0
            ficaTaxableAmount = checkAmount
            maritalStatus = SINGLE
            payPeriodType = WEEKLY
            payrollAllowances = 1
        }

        val medicare = taxModel.medicare
        val socialSecurity = taxModel.socialSecurity
        val taxWithholding = taxModel.taxWithholding
        val federalIncomeTax = taxModel.federalIncomeTax
        federalIncomeTax.apply { withholding = taxWithholding }

        println("TaxModel: ${taxModel.values(taxModel)}")
        print("Medicare: ${medicare.values(medicare)}")
        println("-limit: ${medicare.limit()}")
        println("-amountOfCheck: ${medicare.amountOfCheck()}\n")

        print("SocialSecurity: ${socialSecurity.values(socialSecurity)}")
        println("-amountOfCheck: ${socialSecurity.amountOfCheck()}\n")

        print("TaxWithholding: ${taxWithholding.values(taxWithholding)}")
        println("-individualCost: ${taxWithholding.getIndividualCost()}")
        println("-totalCost: ${taxWithholding.getTotalCost()}\n")

        print("FederalIncomeTax: ${federalIncomeTax.values(federalIncomeTax)}")
        println("-amountOfCheck: ${federalIncomeTax.amountOfCheck()}")
    }

    @Test
    fun testCheckStuff() {
        val check = Check(400.0, PayrollInfo())
        getFederalTaxesWithCheck(check)

        check.addRetirement(Retirement(4.0, true, true))

        println("check amount: ${check.amount}")
        println("retirement: ${check.retirementAmount}")
        println("social security: ${check.socialSecurity}")
        println("medicare: ${check.medicare}")
        println("federal income tax: ${check.federalIncomeTax}")
        println("check after tax: ${check.afterTax}")

        check.updateCheck(400.0)
    }
}
