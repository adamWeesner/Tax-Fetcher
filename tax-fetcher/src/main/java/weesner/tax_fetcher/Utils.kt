package weesner.tax_fetcher

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
/** single */
const val SINGLE = "single"
/** married */
const val MARRIED = "married"
/** "Separate" this used for Married filing separate status */
const val SEPARATE = "Separate"
/** general */
const val GENERAL = "general"
/** nonResident */
const val NON_RESIDENT = "nonResident"

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

fun checkForGsonDependency(): Boolean {
    try {
        Class.forName("com.google.gson")
        return true
    } catch (e: ClassNotFoundException) {
        throw ClassNotFoundException("Gson dependency 'com.google.code.gson:gson:2.8.5' or higher is required for ${FederalTaxes::class.java.`package`.name} to work.")
    }
}

fun getP15Data(file: String): String {
    val classLoader = FederalTaxes::class.java.classLoader
    val stream = classLoader.getResourceAsStream("assets/$file")
    val byte = ByteArray(stream.available())
    stream.read(byte, 0, byte.size)

    return String(byte)
}

fun parseP15FederalIncomeTaxData(info: String): List<ArrayList<FITBracket>> {
    var p15 = info
    p15 = p15.replace("$", "").replace("—", "")
    p15 = p15.replace(" . .", "").replace(",", "")
    p15 = p15.replace("plus ", "").replace("%", "")
    p15 = p15.replace("............ ", "1e1000 ").replace("........... ", "1e1000 ")
    val singleItems = arrayListOf<FITBracket>()
    val marriedItems = arrayListOf<FITBracket>()
    var start = 0
    var end = 1

    var over: Double? = null
    var notOver: Double? = null
    var plus: Double? = null
    var percent: Double? = null
    var taxable: Double? = null
    for (i in 0 until p15.length) {
        if (i != 0 && start == end) start = i - 1
        if (i != 0) end = i

        if (p15[i].toString() == " " || p15[i].toString() == "\n" || i == p15.length - 1) {
            if (i == p15.length - 1) end++
            when {
                over == null -> over = p15.substring(start, end).toDouble()
                notOver == null -> notOver = p15.substring(start, end).toDouble()
                plus == null -> plus = p15.substring(start, end).toDouble()
                percent == null -> percent = p15.substring(start, end).toDouble()
                taxable == null -> taxable = p15.substring(start, end).toDouble()
            }

            start = end
        }

        if (over != null && notOver != null && plus != null && percent != null && taxable != null) {
            if (singleItems.isEmpty()) {
                singleItems.add(FITBracket(over, notOver, plus, percent, taxable))
            } else if (marriedItems.isEmpty()) {
                marriedItems.add(FITBracket(over, notOver, plus, percent, taxable))
            } else {
                if (singleItems.size == marriedItems.size)
                    singleItems.add(FITBracket(over, notOver, plus, percent, taxable))
                else
                    marriedItems.add(FITBracket(over, notOver, plus, percent, taxable))
            }

            over = null
            notOver = null
            plus = null
            percent = null
            taxable = null
        }
    }

    singleItems.add(0, FITBracket(0.0, singleItems[0].over, 0.0, 0.0, 0.0))
    marriedItems.add(0, FITBracket(0.0, marriedItems[0].over, 0.0, 0.0, 0.0))
    return listOf(singleItems, marriedItems)
}

fun parseP15WithholdingData(info: String): ArrayList<String> {
    var p15 = info
    val types = arrayListOf<String>()
    p15 = p15.replace("\n", " ")
    p15 = p15.replace(" $", "").toLowerCase().replace(" or miscellaneous (each day of the payroll period)", "")
    p15 = p15.replace("0 ", "0\n").replace(".", "").replace(",", "").replace("  ", " ")
    var start = 0
    var end = 1
    for (i in 0 until p15.length) {
        if (i != 0 && start == end) start = i - 1
        if (i != 0) end = i

        if (p15[i].toString() == "\n" || i == p15.length - 1) {
            if (i == p15.length - 1) end++
            types.add(p15.substring(start, end))
            start = end
        }
    }

    for (i in 0 until types.size) {
        val item = types[i].replace("\n", "")
        val reformattedItem = "${item.substring(0, item.length - 2).capitalize()}.${item.substring(item.length - 2, item.length)}"

        types.removeAt(i)
        types.add(i, reformattedItem)
    }
    return types
}

fun ArrayList<String>.withholdingToJson(name: String): String {
    var json = "\"$name\": {"
    this.forEach { item ->
        json += "\"${item.substringBefore(" ")}\":${item.substringAfter(" ").toDouble()}"
        if (item != this.last()) json += ","
    }
    json += "}"

    return json
}