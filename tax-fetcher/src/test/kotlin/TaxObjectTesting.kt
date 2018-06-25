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
        val check = Check(942.96, PayrollInfo())
        getFederalTaxesWithCheck(check)

        //val retirement1 = Retirement("401k before", 4.0, false, true)
        //val retirement2 = Retirement("401k after", 4.0, true, false)

        //val deduction1 = PayrollDeduction("dental", 5.00, false, true)

        //check.addRetirement(listOf(retirement1, retirement2))
        //check.addPayrollDeduction(deduction1)

        val fitTaxable = check.ficaTaxable - (check.retirementBeforeTaxesAmount + check.federalTaxes.taxWithholding.getTotalCost())

        println("gross check: ${check.amount}")
        println("--fica taxable: ${check.ficaTaxable}")
        println("--fit taxable: $fitTaxable")
        println("-----------------------------")
        println("retirement: ${check.retirementAmount}")
        println("deductions: ${check.deductionsAmount}")
        println("social security: ${check.socialSecurity - 58.47}")
        println("medicare: ${check.medicare - 13.68}")
        println("federal income tax: ${check.federalIncomeTax - 113.71}")
        println("total: ${check.amountTakenOut() - 240.86}")
        println("-----------------------------")
        println("after tax: ${check.afterTax - 702.10}")
    }
}
