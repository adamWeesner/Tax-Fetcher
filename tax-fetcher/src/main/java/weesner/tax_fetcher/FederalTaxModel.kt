package weesner.tax_fetcher

import kotlin.reflect.full.memberProperties

abstract class FederalTaxModel {
    companion object {
        var yearToDateGross = 0.0
        var checkAmount = 0.0
        var ficaTaxableAmount = 0.0
        var maritalStatus = SINGLE
        var payPeriodType = WEEKLY
        var payrollAllowances = 0
    }

    inline fun <reified T : Any> values(target: T): String {
        var itemList = ""
        T::class.memberProperties.forEach { itemList += "-${it.name}: ${it.get(target)}\n" }
        return itemList
    }
}

class FederalTaxes(val socialSecurity: SocialSecurity, val medicare: Medicare,
                   val federalIncomeTax: FederalIncomeTax, val taxWithholding: TaxWithholding) : FederalTaxModel()

class SocialSecurity(var percent: Double, var limit: Int) : FederalTaxModel() {
    fun amountOfCheck(): Double {
        val ytdSocialSecurity = (percent * .01) * yearToDateGross

        return if (ytdSocialSecurity >= limit) 0.0
        else (percent * .01) * checkAmount
    }
}

class Medicare(var percent: Double, var additional: Double, var limits: HashMap<String, Int>) : FederalTaxModel() {
    var limit = 0

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

    fun amountOfCheck(): Double {
        if (limit == 0) limit()

        val ytdMedicare = (percent * .01) * yearToDateGross

        val percentage =
                if (ytdMedicare >= limit) (percent + additional) * .01
                else percent * .01

        return percentage * checkAmount
    }
}

class TaxWithholding(var general: HashMap<String, Double>, var nonResidents: HashMap<String, Double>) : FederalTaxModel() {
    var individualCost = 0.0

    fun getIndividualCost(type: String = GENERAL): Double {
        payPeriodType.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))

        individualCost = if (type == GENERAL) general[payPeriodType]!!
        else nonResidents[payPeriodType]!!

        return individualCost
    }

    fun getTotalCost(): Double {
        payrollAllowances.toString().validate("Payroll Allowances", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
        return individualCost * payrollAllowances
    }
}

class FederalIncomeTax(var single: HashMap<String, ArrayList<FITBracket>>, var married: HashMap<String, ArrayList<FITBracket>>) : FederalTaxModel() {
    companion object {
        var withholding: TaxWithholding? = null
    }

    fun amountOfCheck(): Double {
        payPeriodType.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))
        maritalStatus.validate("Marital Status", listOf(MARRIED, SINGLE))

        val taxable = checkAmount - withholding!!.getTotalCost()
        println("-taxable: $taxable\n")
        val brackets = when (maritalStatus) {
            SINGLE -> single[payPeriodType]
            MARRIED -> married[payPeriodType]
            else -> throw IllegalArgumentException("Marital Status or Pay Period Type not excepted, check your values.")
        }

        val bracket = brackets!!.first { bracket -> taxable > bracket.over && taxable <= bracket.notOver }
        println("FITBracket: ${bracket.values(bracket)}")

        return (taxable - bracket.nonTaxable) * bracket.percent.toPercentage() + bracket.plus
    }
}

class FITBracket(var over: Double, var notOver: Double, var plus: Double, var percent: Double, var nonTaxable: Double) : FederalTaxModel()