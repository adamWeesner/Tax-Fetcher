package weesner.tax_fetcher

import android.content.Context
import com.google.gson.Gson

/**
 * creates a new FicaTaxObject
 *
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/20/2018
 *
 * @constructor creates a new FicaTaxObject
 *
 * @property context needed to create the FicaTaxObject
 * @property type the payPeriodType of tax; [MEDICARE], [SOCIAL_SECURITY]
 * @property year the year to retrieve the fica tax information for
 * @property maritalStatus the marital status you have chosen; [SINGLE], [MARRIED], [SEPARATE] only needed for 2018+ otherwise will do nothing
 *
 * @throws IllegalArgumentException if the type is not equal to the value of [MEDICARE] or [SOCIAL_SECURITY]
 */
open class FicaTaxObject(var context: Context, var type: String, var year: Int, var maritalStatus: String = SINGLE) {

    /** the retrieved percent for the given [type] and [year] */
    var percent = 0.0

    /** the limit for the fica tax; only implemented for 2018+ */
    var limit = 0

    /** the additional medicare percentage if the year to date amount is over its limit*/
    private var additionalMedicare = 0.0

    /** used exclusively for looping back if [withYearToDateAmount] is called after [forGrossCheckAmount] */
    private var grossCheckAmount = 0.0

    /**
     * the calculated amount of tax to take out of the amount given in .[forGrossCheckAmount]
     * @see forGrossCheckAmount
     */
    var amountOfCheck = 0.0

    /** the year to date amount to calculate the fica limit against; only used if the year is 2018+ */
    var yearToDateAmount = 0.0

    init {
        val validType = type.validate("type", listOf(MEDICARE, SOCIAL_SECURITY))
        val status = maritalStatus.validate("maritalStatus", listOf(SINGLE, MARRIED))

        if (year >= 2018) {
            val jsonObject = Gson().fromJson<TaxModel>(loadJSONFile(context, year.toString()), TaxModel::class.java)
            when (validType) {
                MEDICARE -> {
                    val medicare = jsonObject.medicare
                    percent = medicare.percent
                    additionalMedicare = medicare.additional
                    limit = medicare.forStatus(status)
                }
                SOCIAL_SECURITY -> {
                    val socialSecurity = jsonObject.socialSecurity
                    percent = socialSecurity.percent
                    limit = socialSecurity.limit
                }
            }
        } else {
            val ficaObject = loadJSON(context, validType)
            val ficaItems = ficaObject.getJSONObject(validType)
            percent = ficaItems.getDouble(year.toString())
        }
    }

    /**
     * sets the year to date amount to be used before [forGrossCheckAmount] to calculate fica limits from 2018+
     *
     * @param amount the year to date amount to calculate the fica taxes limit with
     */
    fun withYearToDateAmount(amount: Double): FicaTaxObject {
        yearToDateAmount = amount
        if (grossCheckAmount != 0.0) forGrossCheckAmount(grossCheckAmount)
        return this
    }


    /**
     * calculates the [amountOfCheck] for the given check amount
     *
     * @param amount the amount you want to take the tax out of
     */
    fun forGrossCheckAmount(amount: Double): FicaTaxObject {
        grossCheckAmount = amount
        if (year >= 2018) {
            if (yearToDateAmount >= limit) { // currently this bit of code will never run since there is no way to edit yearToDateAmount from 0.0
                if (type == SOCIAL_SECURITY) {
                    amountOfCheck = 0.0
                } else {
                    val additionalOnAmount = yearToDateAmount - limit // this is the amount to add the additional medicare tax on
                    val normalAmountOfCheck = amount * percent.toPercentage()
                    val additionalAmountOfCheck = additionalOnAmount * additionalMedicare.toPercentage()
                    amountOfCheck = normalAmountOfCheck + additionalAmountOfCheck
                }
            } else {
                amountOfCheck = amount * percent.toPercentage()
            }
        } else {
            amountOfCheck = amount * percent.toPercentage()
        }
        return this
    }
}
