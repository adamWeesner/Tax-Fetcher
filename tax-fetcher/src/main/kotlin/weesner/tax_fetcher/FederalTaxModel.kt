package weesner.tax_fetcher

import com.google.gson.Gson
import java.io.InputStream
import java.util.*

/**
 * Gets the federal taxes for the given [taxesForYear]
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
fun getFederalTaxes(taxesForYear: Int = Calendar.getInstance().get(Calendar.YEAR)): FederalTaxes? {
    val classLoader = FederalTaxes::class.java.classLoader ?: return null
    val stream: InputStream? = classLoader.getResourceAsStream("$taxesForYear.json") ?: return null
    val byte = ByteArray(stream!!.available())
    stream.read(byte, 0, byte.size)
    val taxString = String(byte)

    return Gson().fromJson(taxString, FederalTaxes::class.java)
}

/**
 * An abstract base object for all federal tax objects
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
abstract class FederalTaxModel {
    lateinit var check: Check
    open fun amountOfFicaTaxable(): Double = 0.0
}

/**
 * The object for the JSON file that is gotten from assets
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class FederalTaxes(val socialSecurity: SocialSecurity, val medicare: Medicare,
                   val federalIncomeTax: FederalIncomeTax, val taxWithholding: TaxWithholding) : FederalTaxModel() {
    fun linkCheck(check: Check) {
        this.check = check
        socialSecurity.check = this.check
        medicare.check = this.check
        federalIncomeTax.check = this.check
        taxWithholding.check = this.check
    }
}

/**
 * The Social Security object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class SocialSecurity(var percent: Double, var limit: Int) : FederalTaxModel() {
    /**
     * Gets the amount of the check given to the [FederalTaxModel], if
     * [FederalTaxModel.yearToDateGross] is given it checks to verify that it is not above the max,
     * if it is then no tax will be taken out
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    override fun amountOfFicaTaxable(): Double {
        return if (ytdAmount() >= limit) 0.0
        else percent.asPercent * check.payInfo.ficaTaxable
    }

    /**
     * Gets the year to date amount that has been taken out for the given fica tax, used to
     * determine if the limit of the tax has been reached
     *
     * @author Adam Weesner
     * @since 6/22/2018
     */
    fun ytdAmount(): Double = percent.asPercent * check.yearToDateGross
}

/**
 * The Medicare object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class Medicare(var percent: Double, var additional: Double, var limits: HashMap<String, Int>) : FederalTaxModel() {
    var limit = 0

    /**
     * Gets the limit for the given [FederalTaxModel.maritalStatus]
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    fun limit(): Int {
        limit = limits[check.payrollInfo.maritalStatus] ?: 0
        return limit
    }

    /**
     * Gets the amount of the check given to the [FederalTaxModel], if
     * [FederalTaxModel.yearToDateGross] is given it checks to verify that it is not above the max,
     * if it is then it adds the [additional] amount as well
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    override fun amountOfFicaTaxable(): Double {
        if (limit == 0) limit()
        val percentage =
                if (ytdAmount() >= limit) (percent + additional).asPercent
                else percent.asPercent

        return percentage * check.payInfo.ficaTaxable
    }

    /**
     * Gets the year to date amount that has been taken out for the given fica tax, used to
     * determine if the limit of the tax has been reached
     *
     * @author Adam Weesner
     * @since 6/22/2018
     */
    fun ytdAmount(): Double = percent.asPercent * check.yearToDateGross
}

/**
 * The Tax Withholding object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class TaxWithholding(var general: HashMap<String, Double>, var nonResidents: HashMap<String, Double>) : FederalTaxModel() {
    var individualCost = 0.0

    /**
     * Gets the cost of a single [FederalTaxModel.payrollAllowances]
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    fun getIndividualCost(type: String = GENERAL): Double {
        individualCost = if (type == GENERAL) general[check.payrollInfo.payPeriod]!!
        else nonResidents[check.payrollInfo.payPeriod]!!

        return individualCost
    }

    /**
     * Gets the cost of the amount given for [FederalTaxModel.payrollAllowances]
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    fun getTotalCost(type: String = GENERAL): Double {
        getIndividualCost(type)
        return individualCost * check.payrollInfo.payrollAllowances
    }
}

/**
 * The Federal Income Tax object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class FederalIncomeTax(var single: HashMap<String, ArrayList<FITBracket>>, var married: HashMap<String, ArrayList<FITBracket>>) : FederalTaxModel() {
    companion object {
        /**
         * The [TaxWithholding] needed to determine how much of the given
         * [FederalTaxModel.checkAmount] is taxable
         *
         * @author Adam Weesner
         * @since 6/16/2018
         */
        var withholding: TaxWithholding? = null
    }

    /**
     * Gets the amount of the check given to the [FederalTaxModel] based on the
     * [FederalTaxModel.checkAmount], [FederalTaxModel.maritalStatus],
     * [FederalTaxModel.payPeriodType] and [FederalTaxModel.payrollAllowances]
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    override fun amountOfFicaTaxable(): Double {
        val taxable = check.payInfo.ficaTaxable - (check.retirementBeforeTaxesAmount + withholding!!.getTotalCost())
        val brackets = when (check.payrollInfo.maritalStatus) {
            SINGLE -> single[check.payrollInfo.payPeriod]
            MARRIED -> married[check.payrollInfo.payPeriod]
            else -> throw IllegalArgumentException("Marital Status or Pay Period Type not excepted, check your values.")
        }

        val bracket = brackets!!.first { bracket -> taxable > bracket.over && taxable <= bracket.notOver }
        return ((taxable - bracket.nonTaxable) * bracket.percent.asPercent) + bracket.plus
    }
}

/**
 * The Federal Income Tax Bracket object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class FITBracket(var over: Double, var notOver: Double, var plus: Double, var percent: Double, var nonTaxable: Double) : FederalTaxModel()