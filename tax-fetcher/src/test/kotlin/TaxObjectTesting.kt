import com.google.gson.Gson
import org.junit.Before
import weesner.tax_fetcher.*
import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.yearToDateGross
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
    fun testGson() {
        val taxModelGson = Gson().fromJson<TaxModel>(taxString, TaxModel::class.java)
        println("TaxModel:\n${taxModelGson.values()}")
        println("FederalIncomeTaxObject:\n${taxModelGson.federalIncomeTax.values()}")
        println("FederalIncomeTaxObject-Married-Weekly: ${taxModelGson.federalIncomeTax.forStatus(MARRIED).forPeriod(WEEKLY)}\n")
        println("MedicareObject:\n${taxModelGson.medicare.values()}")
        println("MedicareLimits-Married: ${taxModelGson.medicare.forStatus(MARRIED)}\n")
        println("SocialSecurityObject:\n${taxModelGson.socialSecurity.values()}")
        println("TaxWithholding:\n${taxModelGson.taxWithholding.values()}")
        println("TaxWithholding-General-Weekly: ${taxModelGson.taxWithholding.forType(GENERAL).forPeriod(WEEKLY)}\n")
        println("TaxWithholding-NonResident-BiWeekly: ${taxModelGson.taxWithholding.forType(NON_RESIDENT).forPeriod(BIWEEKLY)}\n\n\n")


        var check = 448.86
        val brackets = taxModelGson.federalIncomeTax.forStatus(SINGLE).forPeriod(WEEKLY)
        for (bracket in brackets) {
            if (check > bracket.over && check <= bracket.notOver) {
                val cost = (check - bracket.nonTaxable) * bracket.percent.toPercentage() + bracket.plus
                println("SelectedBracket-Check:$check:\n" +
                        "-nonTaxable: ${bracket.nonTaxable}\n" +
                        "-notOver: ${bracket.notOver}\n" +
                        "-over: ${bracket.over}\n" +
                        "-percent: ${bracket.percent}\n" +
                        "-plus: ${bracket.plus}\n")
                println("fitCost: $cost")
                break
            }
        }

        assert(taxModelGson != null)
    }

    @Test
    fun testNewGson() {
        val taxModel = Gson().fromJson<FederalTaxes>(taxString, FederalTaxes::class.java)
        taxModel.apply {
            yearToDateGross = 200000.0
            checkAmount = 400.0
            maritalStatus = SINGLE
        }

        val medicare = taxModel.medicare

        println("TaxModel: ${taxModel.values(taxModel)}")
        println("Medicare: ${medicare.values(medicare)}")
        println("-limit: ${medicare.limit()}")
        println("-amountOfCheck: ${medicare.amountOfCheck()}")
    }
}