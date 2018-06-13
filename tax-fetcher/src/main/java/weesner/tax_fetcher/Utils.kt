package weesner.tax_fetcher

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/15/2018
 */

/** allowances */
const val ALLOWANCES = "allowances"
/** medicare */
const val MEDICARE = "medicare"
/** socialSecurity */
const val SOCIAL_SECURITY = "socialSecurity"
/** federalIncomeTax */
const val FEDERAL_INCOME_TAX = "federalIncomeTax"
/** Weekly */
const val WEEKLY = "Weekly"
/** Biweekly */
const val BIWEEKLY = "Biweekly"
/** Semimonthly */
const val SEMIMONTHLY = "Semimonthly"
/** Monthly */
const val MONTHLY = "Monthly"
/** Quarterly */
const val QUARTERLY = "Quarterly"
/** Semiannual */
const val SEMIANNUAL = "Semiannual"
/** Annual */
const val ANNUAL = "Annual"
/** Daily */
const val DAILY = "Daily"
/** over */
const val OVER = "over"
/** notOver */
const val NOT_OVER = "notOver"
/** noMoreThan *///OLD
const val NO_MORE_THAN = "noMoreThan"
/** plus */
const val PLUS = "plus"
/** percent */
const val PERCENT = "percent"
/** nonTaxable */
const val NOT_TAXABLE = "nonTaxable"
/** withheld *///OLD
const val WITHHELD = "withheld"
/** Single */
const val SINGLE = "Single"
/** Married */
const val MARRIED = "Married"
/** "Separate" this used for Married filing separate status */
const val SEPARATE = "Separate"
/** general */
const val GENERAL = "general"
/** nonResident */
const val NON_RESIDENT = "nonResident"

/**
 * list of valid pay period maritalStatus; [WEEKLY], [BIWEEKLY], [SEMIMONTHLY], [MONTHLY], [QUARTERLY], [SEMIANNUAL], [ANNUAL], [DAILY]
 */
val payPeriodTypes = listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY)

/**
 * retrieves the JSON file from the assets folder with the given file name; short of the .json extension
 *
 * @param fileName name of the json file to read from
 * @return          the JSONObject created from getting the JSON file
 */
fun loadJSON(context: Context, fileName: String): JSONObject {
    val sb = StringBuilder()
    try {
        val br = BufferedReader(InputStreamReader(context.resources.assets.open(fileName.toJson())))
        while (br.readLine() != null) sb.append(br.readLine())
        br.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return JSONObject(sb.toString())
}

/**
 * retrieves the JSON file from the assets folder with the given file name; short of the .json extension
 *
 * @param fileName name of the json file to read from
 * @return          the JSONObject created from getting the JSON file
 */
fun loadJSONFile(context: Context, fileName: String): String? {
    try {
        val stream = context.resources.assets.open(fileName.toJson())
        val byte = ByteArray(stream.available())
        stream.read(byte, 0, byte.size)
        return String(byte)
    } catch (e: IOException) {
        return null
    }
}

/** adds .json to the end of the string */
fun String.toJson(): String = "$this.json"

/** converts the double to a percentage */
fun Double.toPercentage(): Double = this / 100

fun String.validate(stringName: String, validItems: List<String>): String {
    var validList = ""
    for (i in 0 until validItems.size) {
        validList += if (i != validItems.size - 1) "${validItems[i]}, "
        else validItems[i]
    }

    if (validItems.contains(this)) return this
    else throw IllegalArgumentException("$stringName can only be one of the following: $validList")
}