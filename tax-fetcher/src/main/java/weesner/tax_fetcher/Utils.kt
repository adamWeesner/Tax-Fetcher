package weesner.tax_fetcher

import android.content.Context
import android.content.res.Resources
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/15/2018
 */
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


/** Gets the JSON file from assets with the given [fileName] */
fun loadJSONFile(fileName: String): String? {
    return try {
        val stream = Resources.getSystem().assets.open(fileName.toJson())
        val byte = ByteArray(stream.available())
        stream.read(byte, 0, byte.size)
        String(byte)
    } catch (e: IOException) {
        null
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