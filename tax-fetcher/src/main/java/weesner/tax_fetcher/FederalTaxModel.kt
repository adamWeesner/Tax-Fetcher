package weesner.tax_fetcher

import android.content.Context
import com.google.gson.Gson
import kotlin.reflect.full.memberProperties

/**
 * Gets the federal taxes for the given [taxesForYear]
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
fun getFederalTaxes(context: Context, taxesForYear: String = "2018"): FederalTaxes {
    val loadJson = taxesForYear.loadTaxFile(context)
    return Gson().fromJson(loadJson, FederalTaxes::class.java)
}

/**
 * An abstract base object for all federal tax objects
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
abstract class FederalTaxModel {
    companion object {
        var yearToDateGross = 0.0
        var checkAmount = 0.0
        var ficaTaxableAmount = 0.0
        var maritalStatus = SINGLE
        var payPeriodType = WEEKLY
        var payrollAllowances = 0
    }

    /**
     * Gets all of the objects parameters and returns them in a string
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    inline fun <reified T : Any> values(target: T): String {
        var itemList = ""
        T::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(target)}\n" }
        return itemList
    }
}

/**
 * The object for the JSON file that is gotten from assets
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class FederalTaxes(val socialSecurity: SocialSecurity, val medicare: Medicare,
                   val federalIncomeTax: FederalIncomeTax, val taxWithholding: TaxWithholding) : FederalTaxModel()

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
    fun amountOfCheck(): Double {
        val ytdSocialSecurity = (percent * .01) * yearToDateGross

        return if (ytdSocialSecurity >= limit) 0.0
        else (percent * .01) * checkAmount
    }
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
        maritalStatus.validate("Marital Status", listOf(MARRIED, SINGLE))

        for (status in limits) {
            if (status.key == maritalStatus) {
                limit = status.value
                break
            }
        }
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
    fun amountOfCheck(): Double {
        if (limit == 0) limit()

        val ytdMedicare = (percent * .01) * yearToDateGross

        val percentage =
                if (ytdMedicare >= limit) (percent + additional) * .01
                else percent * .01

        return percentage * checkAmount
    }
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
        payPeriodType.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))

        individualCost = if (type == GENERAL) general[payPeriodType]!!
        else nonResidents[payPeriodType]!!

        return individualCost
    }

    /**
     * Gets the cost of the amount given for [FederalTaxModel.payrollAllowances]
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    fun getTotalCost(): Double {
        payrollAllowances.toString().validate("Payroll Allowances", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
        return individualCost * payrollAllowances
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
    fun amountOfCheck(): Double {
        payPeriodType.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))
        maritalStatus.validate("Marital Status", listOf(MARRIED, SINGLE))

        val taxable = checkAmount - withholding!!.getTotalCost()
        val brackets = when (maritalStatus) {
            SINGLE -> single[payPeriodType]
            MARRIED -> married[payPeriodType]
            else -> throw IllegalArgumentException("Marital Status or Pay Period Type not excepted, check your values.")
        }

        val bracket = brackets!!.first { bracket -> taxable > bracket.over && taxable <= bracket.notOver }

        return ((taxable - bracket.nonTaxable) * bracket.percent.toPercentage()) + bracket.plus
    }
}

/**
 * The Federal Income Tax Bracket object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class FITBracket(var over: Double, var notOver: Double, var plus: Double, var percent: Double, var nonTaxable: Double) : FederalTaxModel()