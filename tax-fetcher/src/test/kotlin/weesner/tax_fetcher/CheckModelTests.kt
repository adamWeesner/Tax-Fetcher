package weesner.tax_fetcher

import kotlin.test.Test

class CheckModelTests {
    @Test
    fun `Verify that PayInfo amount is calculated correctly`() {
        val pay = PayInfo(10.0, 40.0, 10.0)
        assert(pay.baseAmount == 400.0)
        assert(pay.overtimeRate == 15.0)
        assert(pay.overtimeAmount == 150.0)
        assert(pay.amount == 550.0)
    }

    @Test
    fun `Verify that PayInfo amount can be the only value initialized with`() {
        val pay = PayInfo(400.0)
        assert(pay.amount == 400.0)
    }

    @Test
    fun `Verify that Retirement calculates amount of check properly with percentage`() {
        val retirement = Retirement("My Retirement Fund", 10.0, true)
        assert(retirement.amountOfCheck(400.0) == 40.0)
    }

    @Test
    fun `Verify that Retirement calculates amount of check properly with dollar amount`() {
        val retirement = Retirement("My Retirement Fund", 10.0, false)
        assert(retirement.amountOfCheck(400.0) == 10.0)
    }

    @Test
    fun `Verify that PayrollDeduction calculates amount of check properly with percentage`() {
        val deduction = PayrollDeduction("Deduction", 10.0, true, false)
        assert(deduction.amountOfCheck(400.0) == 40.0)
    }

    @Test
    fun `Verify that PayrollDeduction calculates amount of check properly with dollar amount`() {
        val deduction = PayrollDeduction("My Deduction", 10.0, false, false)
        assert(deduction.amountOfCheck(400.0) == 10.0)
    }

    @Test
    fun `Verify check inits properly`() {
        val retirement = Retirement("My retirement fund", 10.0, true)
        val deduction = PayrollDeduction("my deduction", 1.0, true, true)
        val payInfo = PayInfo(400.0)
        val check = Check(payInfo, PayrollInfo(), retirement, deduction)
        assert(check.healthCareDeductionsAmount == 4.0)
        assert(check.payInfo.ficaTaxable == 396.0)
    }

    @Test
    fun `Verify check calculates taxes`() {
        val payInfo = PayInfo(400.0)
        val check = Check(payInfo, PayrollInfo())
        val federalTaxes = getFederalTaxes(2018)

        check.calculateTaxes(federalTaxes!!)

        assert(check.medicareAmount == 5.8)
        assert(check.socialSecurityAmount == 24.800000000000004)
        assert(check.federalIncomeTaxAmount == 35.82)
        assert(check.federalTaxesAmount == 66.42)
        assert(check.afterTax == 333.58)
    }
}