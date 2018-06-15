package weesner.tax_fetcher

import com.google.gson.annotations.SerializedName
import kotlin.reflect.full.memberProperties

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/27/2018
 */
class TaxModel(
        @SerializedName("socialSecurity") var socialSecurity: SocialSecurity,
        @SerializedName("medicare") var medicare: Medicare,
        @SerializedName("taxWithholding") var taxWithholding: TaxWithholding,
        @SerializedName("federalIncomeTax") var federalIncomeTax: FederalIncomeTax
) {
    class SocialSecurity(var percent: Double, var limit: Int) {
        fun values(): String {
            var itemList = ""
            SocialSecurity::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
            return itemList
        }
    }

    class Medicare(var percent: Double, var additional: Double, @SerializedName("limits") var limits: HashMap<String, Int>) {
        fun forStatus(status: String): Int {
            var amount = -1
            for (limit in limits) {
                if (limit.key == status) {
                    amount = limit.value
                    break
                }
            }
            return amount
        }

        fun values(): String {
            var itemList = ""
            Medicare::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
            return itemList
        }
    }

    class TaxWithholding(@SerializedName("general") var general: HashMap<String, Double>,
                         @SerializedName("nonResidents") var nonResidents: HashMap<String, Double>) {
        var selectedType = HashMap<String, Double>()
        fun forType(type: String = GENERAL): TaxWithholding {
            when (type) {
                GENERAL -> selectedType = general
                NON_RESIDENT -> selectedType = nonResidents
            }
            return this
        }

        fun forPeriod(type: String): Double {
            return selectedType[type]!!
        }

        fun values(): String {
            var itemList = ""
            TaxWithholding::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
            return itemList
        }
    }

    class FederalIncomeTax(@SerializedName("Single") var single: HashMap<String, ArrayList<FITBracket>>,
                           @SerializedName("Married") var married: HashMap<String, ArrayList<FITBracket>>) {
        var selectedStatus = HashMap<String, ArrayList<FITBracket>>()
        fun forStatus(status: String = SINGLE): FederalIncomeTax {
            when (status) {
                SINGLE -> selectedStatus = single
                MARRIED -> selectedStatus = married
            }
            return this
        }

        fun forPeriod(type: String): ArrayList<FITBracket> = selectedStatus[type]!!

        fun values(): String {
            var itemList = ""
            FederalIncomeTax::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
            return itemList
        }
    }

    class FITBracket(var over: Double, var notOver: Double, var plus: Double, var percent: Double, var nonTaxable: Double) {
        fun values(): String {
            var itemList = ""
            FITBracket::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
            return itemList
        }
    }

    fun values(): String {
        var itemList = ""
        TaxModel::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(this)}\n" }
        return itemList
    }
}