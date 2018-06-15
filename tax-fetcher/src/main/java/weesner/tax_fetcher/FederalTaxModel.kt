package weesner.tax_fetcher

import kotlin.reflect.full.memberProperties

abstract class FederalTaxModel {
    companion object {
        var yearToDateGross = 0.0
        var checkAmount = 0.0
        var maritalStatus = SINGLE
    }

    inline fun <reified T : Any> values(target: T): String {
        var itemList = ""
        T::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(target)}\n" }
        return itemList
    }
}

class FederalTaxes(val socialSecurity: SocialSecurity, val medicare: Medicare,
                   val federalIncomeTax: FederalIncomeTax, val taxWithholding: TaxWithholding) : FederalTaxModel()

class SocialSecurity(var percent: Double, var limit: Int) : FederalTaxModel()

class Medicare(var percent: Double, var additional: Double, var limits: HashMap<String, Int>) : FederalTaxModel() {
    var limit = 0

    fun limit(): Int {
        for (status in limits) {
            if (status.key == maritalStatus) {
                limit = status.value
                break
            }
        }
        return limit
    }

    fun amountOfCheck(): Double {
        if (limit == 0) limit()

        val ytdMedicare = (percent * .01) * yearToDateGross

        val percentage =
                if (ytdMedicare >= limit) (percent + additional) * .01
                else percent * .01

        return percentage * checkAmount
    }
}

class TaxWithholding(var general: HashMap<String, Double>, var nonResidents: HashMap<String, Double>) : FederalTaxModel()

class FederalIncomeTax(var single: HashMap<String, ArrayList<FITBracket>>, var married: HashMap<String, ArrayList<FITBracket>>) : FederalTaxModel()

class FITBracket(var over: Double, var notOver: Double, var plus: Double, var percent: Double, var nonTaxable: Double) : FederalTaxModel()