import com.google.gson.Gson
import org.junit.Before
import weesner.tax_fetcher.FederalIncomeTax.Companion.withholding
import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.ficaTaxableAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.payPeriodType
import weesner.tax_fetcher.FederalTaxModel.Companion.payrollAllowances
import weesner.tax_fetcher.FederalTaxModel.Companion.yearToDateGross
import weesner.tax_fetcher.FederalTaxes
import weesner.tax_fetcher.SINGLE
import weesner.tax_fetcher.WEEKLY
import kotlin.test.Test

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/27/2018
 */
class TaxObjectTesting {
    lateinit var taxString: String

    @Before
    fun getResources() {
        val classLoader = javaClass.classLoader
        val stream = classLoader.getResourceAsStream("assets/2018.json")
        val byte = ByteArray(stream.available())
        stream.read(byte, 0, byte.size)
        taxString = String(byte)
    }

    @Test
    fun testNewGson() {
        val taxModel = Gson().fromJson<FederalTaxes>(taxString, FederalTaxes::class.java)
        taxModel.apply {
            yearToDateGross = 2000000.0
            checkAmount = 450.0
            ficaTaxableAmount = checkAmount
            maritalStatus = SINGLE
            payPeriodType = WEEKLY
            payrollAllowances = 2
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
}