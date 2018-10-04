package weesner.tax_fetcher

class Check(
        var payInfo: PayInfo,
        var payrollInfo: PayrollInfo,
        var retirement: List<Retirement>? = null,
        var payrollDeductions: List<PayrollDeduction>? = null
) {

    constructor(payInfo: PayInfo, payrollInfo: PayrollInfo, retirement: Retirement, payrollDeduction: PayrollDeduction)
            : this(payInfo, payrollInfo, listOf(retirement), listOf(payrollDeduction))

    constructor(payInfo: PayInfo, payrollInfo: PayrollInfo, retirement: List<Retirement>? = null, payrollDeduction: PayrollDeduction)
            : this(payInfo, payrollInfo, retirement, listOf(payrollDeduction))

    constructor(payInfo: PayInfo, payrollInfo: PayrollInfo, retirement: Retirement, payrollDeductions: List<PayrollDeduction>? = null)
            : this(payInfo, payrollInfo, listOf(retirement), payrollDeductions)


    /** Total of [healthCareDeductionsAmount] and [nonHealthCareDeductionsAmount] */
    var deductionsAmount: Double = 0.0
    /** Health-care deductions total amount of the [PayInfo.amount], used to get the [PayInfo.ficaTaxable] amount */
    var healthCareDeductionsAmount: Double = 0.0
    /** non-Health-care deductions total amount of the [PayInfo.amount], used to get the [PayInfo.ficaTaxable] amount */
    var nonHealthCareDeductionsAmount: Double = 0.0
    /** Year to date gross amount, used to determine Fica tax maximums. Default: [PayInfo.amount]*/
    var yearToDateGross: Double = payInfo.amount
    /** Total of [retirementAfterTaxesAmount] and [retirementBeforeTaxesAmount] */
    var retirementAmount: Double = 0.0
    /** Retirement amount to be taken out of the [PayInfo.amount] before federal taxes */
    var retirementBeforeTaxesAmount: Double = 0.0
    /** Retirement amount to be taken out of the [PayInfo.amount] after federal taxes */
    var retirementAfterTaxesAmount: Double = 0.0
    /** Amount of medicare tax to be taken out */
    var medicareAmount: Double = 0.0
    /** Amount of social security tax to be taken out */
    var socialSecurityAmount: Double = 0.0
    /** Amount of federal income tax to be taken out */
    var federalIncomeTaxAmount: Double = 0.0
    /** Amount of federal taxes to be taken out */
    var federalTaxesAmount = 0.0
    /** Amount of check after taxes, retirement and deductions, to be taken out */
    var afterTax: Double = 0.0

    init {
        if (payrollDeductions != null) {
            payrollDeductions!!.forEach { deduction ->
                if (deduction.isHealthCare) healthCareDeductionsAmount += deduction.amountOfCheck(payInfo.amount)
                else nonHealthCareDeductionsAmount += deduction.amountOfCheck(payInfo.amount)
            }
        }

        if (retirement != null) {
            retirement!!.forEach { retirement ->
                if (retirement.isTakenBeforeTaxes) retirementBeforeTaxesAmount += retirement.amountOfCheck(payInfo.amount)
            }

            retirementAmount = retirementBeforeTaxesAmount
        }

        payInfo.ficaTaxable = payInfo.amount - healthCareDeductionsAmount
    }

    fun addRetirement(listOfRetirement: List<Retirement>) {
        listOfRetirement.forEach {
            if (it.isTakenBeforeTaxes) retirementBeforeTaxesAmount += it.amountOfCheck(payInfo.amount)
        }

        retirementAmount = retirementBeforeTaxesAmount
    }

    fun addRetirement(retirement: Retirement) {
        addRetirement(listOf(retirement))
    }

    fun addPayrollDeductions(listOfDeductions: List<PayrollDeduction>) {
        listOfDeductions.forEach {
            val amount = it.amountOfCheck(payInfo.amount)

            if (it.isHealthCare) healthCareDeductionsAmount += amount
            else nonHealthCareDeductionsAmount += amount
        }

        payInfo.ficaTaxable = payInfo.amount - healthCareDeductionsAmount

        deductionsAmount = healthCareDeductionsAmount + nonHealthCareDeductionsAmount
    }

    fun addPayrollDeduction(deduction: PayrollDeduction) {
        addPayrollDeductions(listOf(deduction))
    }

    fun calculateTaxes(federalTaxes: FederalTaxes) {
        federalTaxes.linkCheck(this)
        medicareAmount = federalTaxes.medicare.amountOfFicaTaxable()
        socialSecurityAmount = federalTaxes.socialSecurity.amountOfFicaTaxable()

        val taxWithholding = federalTaxes.taxWithholding
        federalIncomeTaxAmount = federalTaxes.federalIncomeTax.apply {
            FederalIncomeTax.withholding = taxWithholding
        }.amountOfFicaTaxable()

        federalTaxesAmount = (federalIncomeTaxAmount + medicareAmount + socialSecurityAmount)

        retirement?.filter { !it.isTakenBeforeTaxes }?.forEach { retirementAfterTaxesAmount += it.amountOfCheck(payInfo.amount - federalTaxesAmount) }
        retirementAmount += retirementAfterTaxesAmount

        afterTax = payInfo.amount - (federalTaxesAmount + deductionsAmount + retirementAmount)
    }

}

class PayInfo(
        var hourlyRate: Double = 0.0,
        var hours: Double = 0.0,
        var overtimeHours: Double = 0.0
) {
    constructor(amount: Double) : this() {
        this.amount = amount
    }

    var baseAmount = hourlyRate * hours
    var overtimeRate = hourlyRate * 1.5
    var overtimeAmount = overtimeRate * overtimeHours
    var amount = baseAmount + overtimeAmount
    var ficaTaxable = 0.0
}

class PayrollInfo(
        var maritalStatus: String = SINGLE,
        var payrollAllowances: Int = 0,
        var payPeriod: String = WEEKLY
) {
    init {
        maritalStatus.validate("Marital Status", listOf(MARRIED, SINGLE))
        payrollAllowances.toString().validate("Payroll Allowances", listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
        payPeriod.validate("Pay Period Type", listOf(WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, DAILY))
    }
}

class Retirement(
        val name: String = "retirementAccount",
        var amount: Double,
        val isPercentage: Boolean,
        val isTakenBeforeTaxes: Boolean = false
) {
    fun amountOfCheck(checkAmount: Double): Double {
        return if (isPercentage) (amount * .01) * checkAmount
        else amount
    }
}

class PayrollDeduction(
        val name: String = "deduction",
        var amount: Double,
        val isPercentage: Boolean,
        val isHealthCare: Boolean
) {
    fun amountOfCheck(checkAmount: Double): Double {
        return if (isPercentage) (amount * .01) * checkAmount
        else amount
    }
}