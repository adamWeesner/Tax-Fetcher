package weesner.tax_fetcher

import android.content.Context
import com.google.gson.Gson
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

    /** only applicable for 2018+, otherwise is null */
    private var jsonYear: TaxModel? = null
    /** only applicable for 2018+, otherwise is null */
    private var fitBrackets: ArrayList<TaxModel.FITBracket>? = null

    init {
        val status = maritalStatus.validate("maritalStatus", listOf(SINGLE, MARRIED))
        val periodType = payPeriodType.validate("payPeriodType", payPeriodTypes)
        val allowType = allowanceType.validate("allowanceType", listOf("general", "nonResident"))
        val allowCount = allowanceCount.toString().validate("allowanceCount", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")).toInt()

        if (year >= 2018) {
            jsonYear = Gson().fromJson<TaxModel>(loadJSONFile(context, year.toString()), TaxModel::class.java)
            fitBrackets = jsonYear!!.federalIncomeTax.forStatus(status).forPeriod(periodType)
            allowanceCost = jsonYear!!.taxWithholding.forType(allowType).forPeriod(periodType)
        } else {
            fetchAllowanceCost()
            try {
                val jsonObject = loadJSON(context, FEDERAL_INCOME_TAX)
                val fitList = jsonObject.getJSONObject(FEDERAL_INCOME_TAX)
                val fitListForYear = fitList.getJSONObject(year.toString())
                fitForTypeAndStatus = fitListForYear.getJSONArray(payPeriodType + "_" + maritalStatus)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        allowancesTotal = allowanceCost * allowCount
    }


    /**
     * calculates the amount federal income tax to be taken out
     * @param amount the amount of the check you want to calculate federal income tax for
     */
    fun forCheckAmount(amount: Double): FederalIncomeTaxObject {
        taxableAmount = amount - allowancesTotal
        if (year >= 2018) {
            for (bracket in fitBrackets!!) {
                if (taxableAmount > bracket.over && taxableAmount <= bracket.notOver) {
                    federalIncomeTaxAmount = (taxableAmount - bracket.nonTaxable) * bracket.percent.toPercentage() + bracket.plus
                    break
                }
            }
        } else {
            for (taxBracket in 0..fitForTypeAndStatus!!.length()) {
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
        return this
    }

    private fun fetchAllowanceCost() {
        try {
            val jsonObject = loadJSON(context, ALLOWANCES)
            val allowancesList = jsonObject.getJSONObject(ALLOWANCES)
            val allowanceItem = allowancesList.getJSONObject(year.toString())
            allowanceCost = allowanceItem.getDouble(payPeriodType)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun fetchCost(fitQualifiers: JSONObject) {
        val plus = fitQualifiers.getDouble(PLUS)
        val percent = fitQualifiers.getDouble(PERCENT).toPercentage()
        val withheld = fitQualifiers.getDouble(WITHHELD)
        federalIncomeTaxAmount = (taxableAmount - withheld) * percent + plus
    }
}