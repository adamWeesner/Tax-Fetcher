package weesner.tax_fetcher

import com.google.gson.Gson
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * Gets the federal taxes for the given [taxesForYear]
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
fun getFederalTaxes(taxesForYear: String = Calendar.getInstance().get(Calendar.YEAR).toString()): FederalTaxes {
    //checkForGsonDependency()

    val classLoader = FederalTaxes::class.java.classLoader
    val stream = classLoader.getResourceAsStream("assets/$taxesForYear.json")
    val byte = ByteArray(stream.available())
    stream.read(byte, 0, byte.size)
    val taxString = String(byte)

    return Gson().fromJson(taxString, FederalTaxes::class.java)
}

fun getFederalTaxesWithCheck(check: Check, year: String = Calendar.getInstance().get(Calendar.YEAR).toString()) {
    val federalTaxes = getFederalTaxes(year)
    check.updateFederalTaxes(federalTaxes)
}

/**
 * An abstract base object for all federal tax objects
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
abstract class FederalTaxModel {
    companion object {
        /**
         * the year to date amount of all of your gross check amounts, used to determine limits for
         *  Medicare and Social Security; not required but if it is not given then Medicare and
         *  Social Security will be calculated regardless if their limit is reached
         */
        var yearToDateGross = 0.0
        /**
         * the amount of your check before any taxes or deductions are removed, aka your gross
         * check amount
         */
        var checkAmount = 0.0
        /**
         * the fica(Social Security and Medicare) taxable amount of your check after healthcare
         * deductions are subtracted out
         */
        var ficaTaxableAmount = 0.0
        /** the marital status on that you put on your W-2 */
        var maritalStatus = SINGLE
        /** how often you get paid */
        var payPeriodType = WEEKLY
        /** the amount of payroll allowances you put on your W-2 */
        var payrollAllowances = 0
        /** the amount in healthcare deductions */
        var healthCareDeductionAmount = 0.0
        /** the amount in retirement before taxes are taken out */
        var retirementBeforeTaxes = 0.0
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

    open fun amountOfCheck(): Double = 0.0
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
 * The Fica parent class for [SocialSecurity] and [Medicare]
 *
 * @author Adam Weesner
 * @since 6/22/2018
 */
open class Fica(var percentage: Double) : FederalTaxModel() {

    /**
     * Gets the fica taxable amount, which is based on the gross check amount minus all healthcare
     * deductions
     *
     * @author Adam Weesner
     * @since 6/22/2018
     */
    fun ficaTaxable(): Double {
        var check =
                if (ficaTaxableAmount == 0.0 && checkAmount != 0.0) checkAmount
                else ficaTaxableAmount

        check -= healthCareDeductionAmount

        return check
    }

    /**
     * Gets the year to date amount that has been taken out for the given fica tax, used to
     * determine if the limit of the tax has been reached
     *
     * @author Adam Weesner
     * @since 6/22/2018
     */
    fun ytdAmount(): Double = (percentage.toPercentage() * yearToDateGross)
}

/**
 * The Social Security object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class SocialSecurity(var percent: Double, var limit: Int) : Fica(percent) {
    /**
     * Gets the amount of the check given to the [FederalTaxModel], if
     * [FederalTaxModel.yearToDateGross] is given it checks to verify that it is not above the max,
     * if it is then no tax will be taken out
     *
     * @author Adam Weesner
     * @since 6/16/2018
     */
    override fun amountOfCheck(): Double {
        return if (ytdAmount() >= limit) 0.0
        else (percent * .01) * ficaTaxable()
    }
}

/**
 * The Medicare object
 *
 * @author Adam Weesner
 * @since 6/16/2018
 */
class Medicare(var percent: Double, var additional: Double, var limits: HashMap<String, Int>) : Fica(percent) {
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
    override fun amountOfCheck(): Double {
        if (limit == 0) limit()
        val percentage =
                if (ytdAmount() >= limit) (percent + additional) * .01
                else percent * .01

        return percentage * ficaTaxable()
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
    fun getTotalCost(type: String = GENERAL): Double {
        payrollAllowances.toString().validate("Payroll Allowances", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
        getIndividualCost(type)
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
    override fun amountOfCheck(): Double {
        payPeriodType.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))
        maritalStatus.validate("Marital Status", listOf(MARRIED, SINGLE))

        val taxable = ficaTaxableAmount - (retirementBeforeTaxes + withholding!!.getTotalCost())
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