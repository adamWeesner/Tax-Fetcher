package weesner.tax_fetcher

import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.ficaTaxableAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.healthCareDeductionAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.payPeriodType
import weesner.tax_fetcher.FederalTaxModel.Companion.payrollAllowances
import weesner.tax_fetcher.FederalTaxModel.Companion.retirementBeforeTaxes
import weesner.tax_fetcher.FederalTaxModel.Companion.yearToDateGross

class Check(
        var amount: Double,
        var payInfo: PayrollInfo
) {
    var ficaTaxable: Double = amount
    var yearToDateAmount: Double = amount

    var retirementAmount: Double = 0.0
    var retirementBeforeTaxesAmount: Double = 0.0
    private var retirementAfterTaxesAmount: Double = 0.0

    var deductionsAmount: Double = 0.0
    var healthCareDeductionsAmount: Double = 0.0
    var nonHealthCareDeductionsAmount: Double = 0.0

    var medicare: Double = 0.0
    var socialSecurity: Double = 0.0
    var federalIncomeTax: Double = 0.0
    private var federalTaxesAmount = 0.0
    var afterTax: Double = amount

    lateinit var federalTaxes: FederalTaxes

    fun updateFederalTaxes(federalTaxes: FederalTaxes) {
        this.federalTaxes = federalTaxes
        updateTaxInfo()
        calculateTaxes()
        afterTax = amount - (federalTaxesAmount - retirementAmount)
    }

    fun updateCheck(amount: Double) {
        this.amount = amount

        medicare = 0.0
        socialSecurity = 0.0
        federalIncomeTax = 0.0
        federalTaxesAmount = 0.0

        updateTaxInfo()

        if (amount != 0.0) calculateTaxes()
        afterTax = amount - (federalTaxesAmount - retirementAmount)
    }

    fun addRetirement(listOfRetirement: List<Retirement>) {
        listOfRetirement.forEach {
            if (it.isTakenBeforeTaxes) {
                retirementBeforeTaxesAmount += it.amountOfCheck(amount)
            } else {
                if (federalTaxesAmount == 0.0) calculateTaxes()
                retirementAfterTaxesAmount += it.amountOfCheck(amount - federalTaxesAmount)
            }
        }

        retirementAmount = retirementBeforeTaxesAmount + retirementAfterTaxesAmount
        updateTaxInfo()
    }

    fun addRetirement(retirement: Retirement) {
        addRetirement(listOf(retirement))
    }

    fun addPayrollDeductions(listOfDeductions: List<PayrollDeduction>) {
        listOfDeductions.forEach {
            val amount = it.amountOfCheck(amount)

            if (it.isHealthCare) healthCareDeductionsAmount += amount
            else nonHealthCareDeductionsAmount += amount
        }

        ficaTaxable = amount - healthCareDeductionsAmount

        deductionsAmount = healthCareDeductionsAmount + nonHealthCareDeductionsAmount
        updateTaxInfo()
    }

    fun addPayrollDeduction(deduction: PayrollDeduction) {
        addPayrollDeductions(listOf(deduction))
    }

    private fun updateTaxInfo() {
        if (yearToDateGross != yearToDateAmount)
            federalTaxes.apply { yearToDateGross = yearToDateAmount }

        if (ficaTaxableAmount != ficaTaxable)
            federalTaxes.apply { ficaTaxableAmount = ficaTaxable }

        if (healthCareDeductionAmount != healthCareDeductionsAmount)
            federalTaxes.apply { healthCareDeductionAmount = healthCareDeductionsAmount }

        if (checkAmount != amount)
            federalTaxes.apply { checkAmount = amount }

        if (maritalStatus != payInfo.maritalStatus)
            federalTaxes.apply { maritalStatus = payInfo.maritalStatus }

        if (payPeriodType != payInfo.payPeriod)
            federalTaxes.apply { payPeriodType = payInfo.payPeriod }

        if (payrollAllowances != payInfo.payrollAllowances)
            federalTaxes.apply { payrollAllowances = payInfo.payrollAllowances }

        if (retirementBeforeTaxes != retirementBeforeTaxesAmount)
            federalTaxes.apply { retirementBeforeTaxes = retirementBeforeTaxesAmount }
    }

    private fun calculateTaxes() {
        medicare = federalTaxes.medicare.amountOfCheck()
        socialSecurity = federalTaxes.socialSecurity.amountOfCheck()

        val taxWithholding = federalTaxes.taxWithholding
        federalIncomeTax = federalTaxes.federalIncomeTax.apply {
            FederalIncomeTax.withholding = taxWithholding
        }.amountOfCheck()

        federalTaxesAmount = (federalIncomeTax + medicare + socialSecurity)
    }

    fun amountTakenOut(): Double = federalTaxesAmount + retirementAmount
}

class PayrollInfo(
        var maritalStatus: String = SINGLE,
        var payrollAllowances: Int = 0,
        var payPeriod: String = WEEKLY
)

class Retirement(
        val name: String = "retirementAccount",
        var amount: Double,
        val isPercentage: Boolean,
        val isTakenBeforeTaxes: Boolean = false
) {
    fun amountOfCheck(checkAmount: Double): Double {
        return if (isPercentage) (amount * .01) * checkAmount
        else amount
    }
}

class PayrollDeduction(
        val name: String = "deduction",
        var amount: Double,
        val isPercentage: Boolean,
        val isHealthCare: Boolean
) {
    fun amountOfCheck(checkAmount: Double): Double {
        return if (isPercentage) (amount * .01) * checkAmount
        else amount
    }
}