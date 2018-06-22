package weesner.tax_fetcher

import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.ficaTaxableAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.payPeriodType
import weesner.tax_fetcher.FederalTaxModel.Companion.payrollAllowances
import weesner.tax_fetcher.FederalTaxModel.Companion.yearToDateGross

class Check(var amount: Double, var payInfo: PayInfo) {
    var ficaTaxable: Double = 0.0
    var yearToDateAmount: Double = amount

    var medicare: Double = 0.0
    var socialSecurity: Double = 0.0
    var federalIncomeTax: Double = 0.0
    var afterTax: Double = 0.0

    lateinit var federalTaxes: FederalTaxes

    fun updateCheck(amount: Double) {
        this.amount = amount

        medicare = 0.0
        socialSecurity = 0.0
        federalIncomeTax = 0.0

        if (amount != 0.0)
            calculateTaxes()
    }

    private fun updateTaxInfo() {
        println("yearToDate same?: $yearToDateGross | $yearToDateAmount | ${(yearToDateGross == yearToDateAmount)}")
        println("ficaTaxable same?: $ficaTaxableAmount | $ficaTaxable | ${(ficaTaxableAmount == ficaTaxableAmount)}")
        println("checkAmount same?: $checkAmount | $amount | ${(checkAmount == amount)}")
        println("maritalStatus same?: $maritalStatus | ${payInfo.maritalStatus} | ${(maritalStatus == payInfo.maritalStatus)}")
        println("payPeriod same?: $payPeriodType | ${payInfo.payPeriod} | ${(payPeriodType == payInfo.payPeriod)}")
        println("allowances same?: $payrollAllowances | ${payInfo.payrollAllowances}| ${(payrollAllowances == payInfo.payrollAllowances)}")

        if (yearToDateGross != yearToDateAmount)
            federalTaxes.apply { yearToDateGross = yearToDateAmount }

        if (ficaTaxableAmount != ficaTaxable)
            federalTaxes.apply { ficaTaxableAmount = ficaTaxable }

        if (checkAmount != amount)
            federalTaxes.apply { checkAmount = amount }

        if (maritalStatus != payInfo.maritalStatus)
            federalTaxes.apply { maritalStatus = payInfo.maritalStatus }

        if (payPeriodType != payInfo.payPeriod)
            federalTaxes.apply { payPeriodType = payInfo.payPeriod }

        if (payrollAllowances != payInfo.payrollAllowances)
            federalTaxes.apply { payrollAllowances = payInfo.payrollAllowances }
    }

    fun calculateTaxes() {
        updateTaxInfo()

        medicare = federalTaxes.medicare.amountOfCheck()
        socialSecurity = federalTaxes.socialSecurity.amountOfCheck()
        val taxWithholding = federalTaxes.taxWithholding
        federalIncomeTax = federalTaxes.federalIncomeTax.apply {
            FederalIncomeTax.withholding = taxWithholding
        }.amountOfCheck()

        afterTax = amount - (federalIncomeTax + medicare + socialSecurity)
    }
}

class PayInfo(var maritalStatus: String = SINGLE,
              var payrollAllowances: Int = 0,
              var payPeriod: String = WEEKLY)