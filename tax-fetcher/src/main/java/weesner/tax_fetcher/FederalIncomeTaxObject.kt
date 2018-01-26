package weesner.tax_fetcher

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * creates a FederalIncomeTaxObject
 *
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/20/2018
 *
 * @constructor creates a FederalIncomeTaxObject
 *
 * @property context needed to create the FederalIncomeTaxObject
 * @property year the year to retrieve the federal income tax for
 * @property allowanceCount the number of allowances you have
 * @property payPeriodType the pay period type; equal to one of the values in [payPeriodTypes]
 * @property maritalStatus the marital status; equal to the value of [MARRIED] or [SINGLE]; Head of Household uses the same FIT brackets as Single
 * @property allowanceType the tax withholding type, either "general"(default) or "nonResident" for non-resident aliens
 *
 * @throws IllegalArgumentException if [allowanceCount] is more than 10
 * @throws IllegalArgumentException if the [payPeriodType] is not equal to one of the values in [payPeriodTypes]
 *
 * @sample FederalIncomeTaxObject(this, 2018, 1, [WEEKLY], [SINGLE])
 */
class FederalIncomeTaxObject(var context: Context, var year: Int, var allowanceCount: Int, var payPeriodType: String, var maritalStatus: String, var allowanceType: String = "general") {

    /** the cost of each allowance for the given [payPeriodType] and [year] */
    var allowanceCost = 0.0

    /**
     * the total [allowanceCost] of all the [allowanceCount]
     * @see allowanceCost
     */
    var allowancesTotal = 0.0

    /**
     * the taxable amount of the value given in [forCheckAmount]
     * @see forCheckAmount
     */
    var taxableAmount = 0.0

    /** the amount of federal income tax taken out of the given check amount in [forCheckAmount]
     * @see forCheckAmount
     */
    var federalIncomeTaxAmount = 0.0

    private var fitForTypeAndStatus: JSONArray? = null

    /** only applicable for 2018+, otherwise is an empty object */
    private val jsonYear: JSONObject

    init {
        if (year >= 2018) jsonYear = loadJSON(context, year.toString().toJson())
        else jsonYear = JSONObject()

        fetchAllowanceCost(allowanceType)
        allowancesTotal =
                if (allowanceCount < 10) allowanceCost * allowanceCount
                else throw IllegalArgumentException("allowanceCount parameter has to be less than 10")

        try {
            if (year >= 2018) {
                val fitBaseObject = jsonYear.getJSONObject(FEDERAL_INCOME_TAX)
                val fitForStatus = fitBaseObject.getJSONObject(maritalStatus)
                fitForTypeAndStatus = fitForStatus.getJSONArray(payPeriodType)
            } else {
                val jsonObject = loadJSON(context, FEDERAL_INCOME_TAX)
                val fitList = jsonObject.getJSONObject(FEDERAL_INCOME_TAX)
                val fitListForYear = fitList.getJSONObject(year.toString())
                fitForTypeAndStatus = fitListForYear.getJSONArray(payPeriodType + "_" + maritalStatus)
            }
        } catch (e: JSONException) {
            throw e
        }
    }


    /**
     * calculates the amount federal income tax to be taken out
     * @param amount the amount of the check you want to calculate federal income tax for
     */
    fun forCheckAmount(amount: Double) {
        taxableAmount = amount - allowancesTotal
        for (taxBracket in 0..fitForTypeAndStatus!!.length()) {
            if (year >= 2018) {
                try {
                    val fitQualifiers = fitForTypeAndStatus!!.getJSONObject(taxBracket)
                    if (taxableAmount > fitQualifiers.getDouble(OVER) && taxableAmount < fitQualifiers.getDouble(NOT_OVER)) {
                        val plus = fitQualifiers.getDouble(PLUS)
                        val percent = fitQualifiers.getDouble(PERCENT).toPercentage()
                        val notTaxable = fitQualifiers.getDouble(NOT_TAXABLE)
                        federalIncomeTaxAmount = (taxableAmount - notTaxable) * percent + plus
                        break
                    }
                } catch (nfe: NumberFormatException) {
                    if (nfe.message == context.getString(R.string.invalid_double_error)) {
                        val fitQualifiers = fitForTypeAndStatus!!.getJSONObject(taxBracket)
                        val plus = fitQualifiers.getDouble(PLUS)
                        val percent = fitQualifiers.getDouble(PERCENT).toPercentage()
                        val notTaxable = fitQualifiers.getDouble(NOT_TAXABLE)
                        federalIncomeTaxAmount = (taxableAmount - notTaxable) * percent + plus
                    }
                }
            } else {
                try {
                    val fitQualifiers = fitForTypeAndStatus!!.getJSONObject(taxBracket)
                    if (taxableAmount <= fitQualifiers.getDouble(NO_MORE_THAN)) {
                        fetchCost(fitQualifiers)
                        break
                    }
                } catch (nfe: NumberFormatException) {
                    if (nfe.message == context.getString(R.string.invalid_double_error)) {
                        val fitQualifiers = fitForTypeAndStatus!!.getJSONObject(taxBracket)
                        fetchCost(fitQualifiers)
                    }
                }
            }
        }

    }

    private fun fetchAllowanceCost(allowanceType: String) {
        if (payPeriodTypes.contains(payPeriodType)) {
            try {
                if (year >= 2018) {
                    val allowancesList = jsonYear.getJSONObject("taxWithholding")
                    val withholdingType = allowancesList.getJSONObject(allowanceType)
                    allowanceCost = withholdingType.getDouble(payPeriodType)
                } else {
                    val jsonObject = loadJSON(context, ALLOWANCES)
                    val allowancesList = jsonObject.getJSONObject(ALLOWANCES)
                    val allowanceItem = allowancesList.getJSONObject(year.toString())
                    allowanceCost = allowanceItem.getDouble(payPeriodType)
                }
            } catch (e: JSONException) {
                throw e
            }
        } else {
            throw IllegalArgumentException("$payPeriodType is not a valid pay period payPeriodType...")
        }
    }

    private fun fetchCost(fitQualifiers: JSONObject) {
        val plus = fitQualifiers.getDouble(PLUS)
        val percent = fitQualifiers.getDouble(PERCENT).toPercentage()
        val withheld = fitQualifiers.getDouble(WITHHELD)
        federalIncomeTaxAmount = (taxableAmount - withheld) * percent + plus
    }
}