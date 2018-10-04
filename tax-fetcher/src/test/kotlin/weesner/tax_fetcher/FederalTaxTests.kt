package weesner.tax_fetcher

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FederalTaxTests {
    @Nested
    inner class Taxes2015 {
        val year = 2015

        @Test
        fun `Verify cannot get federal taxes`() {
            assert(getFederalTaxes(year) == null)
        }

        @Test
        fun `Verify Medicare`() {
            val federalTaxes = getFederalTaxes(year)
            assert(federalTaxes?.medicare == null)
        }

        @Test
        fun `Verify Social Security`() {
            val federalTaxes = getFederalTaxes(year)
            assert(federalTaxes?.socialSecurity == null)
        }

        @Test
        fun `Verify Federal Withholding`() {
            val federalTaxes = getFederalTaxes(year)
            assert(federalTaxes?.taxWithholding == null)
        }

        @Test
        fun `Verify Federal Income Tax`() {
            val federalTaxes = getFederalTaxes(year)
            assert(federalTaxes?.federalIncomeTax == null)
        }
    }

    @Nested
    inner class Taxes2016 {
        val year = 2016

        @Test
        fun `Verify can get federal taxes`() {
            assert(getFederalTaxes(year) != null)
        }

        @Test
        fun `Verify Federal Income Tax`() {
            val federalTaxes = getFederalTaxes(year)
            assert(federalTaxes?.federalIncomeTax != null)
        }

        @Nested
        inner class TaxWithholding {
            @Test
            fun Verify() {
                val federalTaxes = getFederalTaxes(year)
                assert(federalTaxes?.taxWithholding != null)
            }

            @Test
            fun `Verify individual cost`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val socialSecurity = taxes.taxWithholding
                assert(socialSecurity.getIndividualCost() == 77.9)
            }

            @Test
            fun `Verify total cost`() {
                val check = Check(PayInfo(400.0), PayrollInfo(payrollAllowances = 2))
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val socialSecurity = taxes.taxWithholding
                assert(socialSecurity.getTotalCost() == 155.8)
            }
        }

        @Nested
        inner class SocialSecurity {
            @Test
            fun Verify() {
                val federalTaxes = getFederalTaxes(year)
                assert(federalTaxes?.socialSecurity != null)
            }

            @Test
            fun `Verify YTD amount`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val socialSecurity = taxes.socialSecurity
                assert(socialSecurity.ytdAmount() == 24.800000000000004)
            }

            @Test
            fun `Verify fica taxable amount`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val socialSecurity = taxes.socialSecurity
                assert(socialSecurity.amountOfFicaTaxable() == 24.800000000000004)
            }
        }

        @Nested
        inner class Medicare {
            @Test
            fun Verify() {
                val federalTaxes = getFederalTaxes(year)
                assert(federalTaxes?.medicare != null)
            }

            @Test
            fun `Verify YTD amount`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val medicare = taxes.medicare
                assert(medicare.ytdAmount() == 5.8)
            }

            @Test
            fun `Verify limit amount`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val medicare = taxes.medicare
                assert(medicare.limit() == 200000)
            }

            @Test
            fun `Verify fica taxable amount`() {
                val check = Check(PayInfo(400.0), PayrollInfo())
                val taxes = getFederalTaxes(year)!!
                taxes.linkCheck(check)
                val medicare = taxes.medicare
                assert(medicare.amountOfFicaTaxable() == 5.8)
            }
        }
    }
}