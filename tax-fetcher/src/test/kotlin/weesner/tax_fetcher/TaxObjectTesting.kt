import kotlin.test.Test

/**
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/27/2018
 */
class TaxObjectTesting {
    @Test
    fun testNewGson() {
        /*val taxModel = getFederalTaxes()
        taxModel.apply {
            //yearToDateGross = 2000000.0
            checkAmount = 1211.6999999999998
            ficaTaxableAmount = checkAmount
            maritalStatus = SINGLE
            payPeriodType = WEEKLY
            payrollAllowances = 0
        }

        val medicareAmount = taxModel.medicareAmount
        val socialSecurityAmount = taxModel.socialSecurityAmount
        val taxWithholding = taxModel.taxWithholding
        val federalIncomeTaxAmount = taxModel.federalIncomeTaxAmount
        federalIncomeTaxAmount.apply { withholding = taxWithholding }

        println("TaxModel: ${taxModel.values(taxModel)}")
        print("Medicare: ${medicareAmount.values(medicareAmount)}")
        println("-limit: ${medicareAmount.limit()}")
        println("-amountOfCheck: ${medicareAmount.amountOfCheck()}\n")

        print("SocialSecurity: ${socialSecurityAmount.values(socialSecurityAmount)}")
        println("-amountOfCheck: ${socialSecurityAmount.amountOfCheck()}\n")

        print("TaxWithholding: ${taxWithholding.values(taxWithholding)}")
        println("-individualCost: ${taxWithholding.getIndividualCost()}")
        println("-totalCost: ${taxWithholding.getTotalCost()}\n")

        print("FederalIncomeTax: ${federalIncomeTaxAmount.values(federalIncomeTaxAmount)}")
        println("-amountOfCheck: ${federalIncomeTaxAmount.amountOfCheck()}")*/
    }

    @Test
    fun testCheckStuff() {
        /*//val check = Check(baseCheck + overtime, PayrollInfo())
        val check = Check(payInfo = PayInfo(17.31, 40.0, 0.0), payrollInfo = PayrollInfo())
        getFederalTaxesWithCheck(check)

        val fitTaxable = check.ficaTaxable - (check.retirementBeforeTaxesAmount + check.federalTaxes.taxWithholding.getTotalCost())

        println("gross check: ${check.amount}")
        println("--fica taxable: ${check.ficaTaxable}")
        println("--fit taxable: $fitTaxable")
        println("-----------------------------")
        println("retirement: ${check.retirementAmount}")
        println("deductions: ${check.deductionsAmount}")
        println("social security: ${check.socialSecurityAmount}")
        println("medicareAmount: ${check.medicareAmount}")
        println("federal income tax: ${check.federalIncomeTaxAmount}")
        println("total: ${check.amountTakenOut()}")
        println("-----------------------------")
        println("after tax: ${check.afterTax}")*/
    }
/*
    @Test
    fun testGettingP15Withholding() {
        val general = parseP15WithholdingData("Weekly .......................... \$ 79.80\n" +
                "Biweekly ......................... 159.60\n" +
                "Semimonthly ...................... 172.90\n" +
                "Monthly .......................... 345.80\n" +
                "Quarterly ......................... 1,037.50\n" +
                "Semiannually ...................... 2,075.00\n" +
                "Annually ......................... 4,150.00\n" +
                "Daily or miscellaneous (each day of the payroll\n" +
                "period) .......................... 16.00")
        val nonResidents = parseP15WithholdingData("Weekly \$ 151.00\n" +
                "Biweekly 301.90\n" +
                "Semimonthly 327.10\n" +
                "Monthly 654.20\n" +
                "Quarterly 1,962.50\n" +
                "Semiannually 3,925.00\n" +
                "Annually 7,850.00\n" +
                "Daily or Miscellaneous (each\n" +
                "day of the payroll period)\n" +
                "30.20\n")

        println(general.withholdingToJson("general"))
        println()
        println(nonResidents.withholdingToJson("nonResidents"))
    }

    @Test
    fun testGettingP15FederalIncomeTax() {
        val brackets = hashMapOf<String, ArrayList<FITBracket>>()
        val weekly = parseP15FederalIncomeTaxData("\$71 —\$254 . . \$0.00 plus 10% —\$71 \$222 —\$588 . . \$0.00 plus 10% —\$222\n" +
                "\$254 —\$815 . . \$18.30 plus 12% —\$254 \$588 —\$1,711 . . \$36.60 plus 12% —\$588\n" +
                "\$815 —\$1,658 . . \$85.62 plus 22% —\$815 \$1,711 —\$3,395 . . \$171.36 plus 22% —\$1,711\n" +
                "\$1,658 —\$3,100 . . \$271.08 plus 24% —\$1,658 \$3,395 —\$6,280 . . \$541.84 plus 24% —\$3,395\n" +
                "\$3,100 —\$3,917 . . \$617.16 plus 32% —\$3,100 \$6,280 —\$7,914 . . \$1,234.24 plus 32% —\$6,280\n" +
                "\$3,917 —\$9,687 . . \$878.60 plus 35% —\$3,917 \$7,914 —\$11,761 . . \$1,757.12 plus 35% —\$7,914\n" +
                "\$9,687 ............ \$2,898.10 plus 37% —\$9,687 \$11,761 ........... \$3,103.57 plus 37% —\$11,761\n")
        val biWeekly = parseP15FederalIncomeTaxData("\$142 —\$509 . . \$0.00 plus 10% —\$142 \$444 —\$1,177 . . \$0.00 plus 10% —\$444\n" +
                "\$509 —\$1,631 . . \$36.70 plus 12% —\$509 \$1,177 —\$3,421 . . \$73.30 plus 12% —\$1,177\n" +
                "\$1,631 —\$3,315 . . \$171.34 plus 22% —\$1,631 \$3,421 —\$6,790 . . \$342.58 plus 22% —\$3,421\n" +
                "\$3,315 —\$6,200 . . \$541.82 plus 24% —\$3,315 \$6,790 —\$12,560 . . \$1,083.76 plus 24% —\$6,790\n" +
                "\$6,200 —\$7,835 . . \$1,234.22 plus 32% —\$6,200 \$12,560 —\$15,829 . . \$2,468.56 plus 32% —\$12,560\n" +
                "\$7,835 —\$19,373 . . \$1,757.42 plus 35% —\$7,835 \$15,829 —\$23,521 . . \$3,514.64 plus 35% —\$15,829\n" +
                "\$19,373 ............ \$5,795.72 plus 37% —\$19,373 \$23,521 ........... \$6,206.84 plus 37% —\$23,521\n")
        val semiMonthly = parseP15FederalIncomeTaxData("\$154 —\$551 . . \$0.00 plus 10% —\$154 \$481 —\$1,275 . . \$0.00 plus 10% —\$481\n" +
                "\$551 —\$1,767 . . \$39.70 plus 12% —\$551 \$1,275 —\$3,706 . . \$79.40 plus 12% —\$1,275\n" +
                "\$1,767 —\$3,592 . . \$185.62 plus 22% —\$1,767 \$3,706 —\$7,356 . . \$371.12 plus 22% —\$3,706\n" +
                "\$3,592 —\$6,717 . . \$587.12 plus 24% —\$3,592 \$7,356 —\$13,606 . . \$1,174.12 plus 24% —\$7,356\n" +
                "\$6,717 —\$8,488 . . \$1,337.12 plus 32% —\$6,717 \$13,606 —\$17,148 . . \$2,674.12 plus 32% —\$13,606\n" +
                "\$8,488 —\$20,988 . . \$1,903.84 plus 35% —\$8,488 \$17,148 —\$25,481 . . \$3,807.56 plus 35% —\$17,148\n" +
                "\$20,988 ............ \$6,278.84 plus 37% —\$20,988 \$25,481 ........... \$6,724.11 plus 37% —\$25,481\n")
        val monthly = parseP15FederalIncomeTaxData("\$308 —\$1,102 . . \$0.00 plus 10% —\$308 \$963 —\$2,550 . . \$0.00 plus 10% —\$963\n" +
                "\$1,102 —\$3,533 . . \$79.40 plus 12% —\$1,102 \$2,550 —\$7,413 . . \$158.70 plus 12% —\$2,550\n" +
                "\$3,533 —\$7,183 . . \$371.12 plus 22% —\$3,533 \$7,413 —\$14,713 . . \$742.26 plus 22% —\$7,413\n" +
                "\$7,183 —\$13,433 . . \$1,174.12 plus 24% —\$7,183 \$14,713 —\$27,213 . . \$2,348.26 plus 24% —\$14,713\n" +
                "\$13,433 —\$16,975 . . \$2,674.12 plus 32% —\$13,433 \$27,213 —\$34,296 . . \$5,348.26 plus 32% —\$27,213\n" +
                "\$16,975 —\$41,975 . . \$3,807.56 plus 35% —\$16,975 \$34,296 —\$50,963 . . \$7,614.82 plus 35% —\$34,296\n" +
                "\$41,975 ............ \$12,557.56 plus 37% —\$41,975 \$50,963 ........... \$13,448.27 plus 37% —\$50,963")
        val quarterly = parseP15FederalIncomeTaxData("\$925 —\$3,306 . . \$0.00 plus 10% —\$925 \$2,888 —\$7,650 . . \$0.00 plus 10% —\$2,888\n" +
                "\$3,306 —\$10,600 . . \$238.10 plus 12% —\$3,306 \$7,650 —\$22,238 . . \$476.20 plus 12% —\$7,650\n" +
                "\$10,600 —\$21,550 . . \$1,113.38 plus 22% —\$10,600 \$22,238 —\$44,138 . . \$2,226.76 plus 22% —\$22,238\n" +
                "\$21,550 —\$40,300 . . \$3,522.38 plus 24% —\$21,550 \$44,138 —\$81,638 . . \$7,044.76 plus 24% —\$44,138\n" +
                "\$40,300 —\$50,925 . . \$8,022.38 plus 32% —\$40,300 \$81,638 —\$102,888 . . \$16,044.76 plus 32% —\$81,638\n" +
                "\$50,925 —\$125,925 . . \$11,422.38 plus 35% —\$50,925 \$102,888 —\$152,888 . . \$22,844.76 plus 35% —\$102,888\n" +
                "\$125,925 ........... \$37,672.38 plus 37% —\$125,925 \$152,888 ........... \$40,344.76 plus 37% —\$152,888")
        val semiAnnual = parseP15FederalIncomeTaxData("\$1,850 —\$6,613 . . \$0.00 plus 10% —\$1,850 \$5,775 —\$15,300 . . \$0.00 plus 10% —\$5,775\n" +
                "\$6,613 —\$21,200 . . \$476.30 plus 12% —\$6,613 \$15,300 —\$44,475 . . \$952.50 plus 12% —\$15,300\n" +
                "\$21,200 —\$43,100 . . \$2,226.74 plus 22% —\$21,200 \$44,475 —\$88,275 . . \$4,453.50 plus 22% —\$44,475\n" +
                "\$43,100 —\$80,600 . . \$7,044.74 plus 24% —\$43,100 \$88,275 —\$163,275 . . \$14,089.50 plus 24% —\$88,275\n" +
                "\$80,600 —\$101,850 . . \$16,044.74 plus 32% —\$80,600 \$163,275 —\$205,775 . . \$32,089.50 plus 32% —\$163,275\n" +
                "\$101,850 —\$251,850 . . \$22,844.74 plus 35% —\$101,850 \$205,775 —\$305,775 . . \$45,689.50 plus 35% —\$205,775\n" +
                "\$251,850 ........... \$75,344.74 plus 37% —\$251,850 \$305,775 ........... \$80,689.50 plus 37% —\$305,775\n")
        val annual = parseP15FederalIncomeTaxData("\$3,700 —\$13,225 . . \$0.00 plus 10% —\$3,700 \$11,550 —\$30,600 . . \$0.00 plus 10% —\$11,550\n" +
                "\$13,225 —\$42,400 . . \$952.50 plus 12% —\$13,225 \$30,600 —\$88,950 . . \$1,905.00 plus 12% —\$30,600\n" +
                "\$42,400 —\$86,200 . . \$4,453.50 plus 22% —\$42,400 \$88,950 —\$176,550 . . \$8,907.00 plus 22% —\$88,950\n" +
                "\$86,200 —\$161,200 . . \$14,089.50 plus 24% —\$86,200 \$176,550 —\$326,550 . . \$28,179.00 plus 24% —\$176,550\n" +
                "\$161,200 —\$203,700 . . \$32,089.50 plus 32% —\$161,200 \$326,550 —\$411,550 . . \$64,179.00 plus 32% —\$326,550\n" +
                "\$203,700 —\$503,700 . . \$45,689.50 plus 35% —\$203,700 \$411,550 —\$611,550 . . \$91,379.00 plus 35% —\$411,550\n" +
                "\$503,700 ........... \$150,689.50 plus 37% —\$503,700 \$611,550 ........... \$161,379.00 plus 37% —\$611,550\n")
        val daily = parseP15FederalIncomeTaxData("\$14.20 —\$50.90 . . \$0.00 plus 10% —\$14.20 \$44.40 —\$117.70 . . \$0.00 plus 10% —\$44.40\n" +
                "\$50.90 —\$163.10 . . \$3.67 plus 12% —\$50.90 \$117.70 —\$342.10 . . \$7.33 plus 12% —\$117.70\n" +
                "\$163.10 —\$331.50 . . \$17.13 plus 22% —\$163.10 \$342.10 —\$679.00 . . \$34.26 plus 22% —\$342.10\n" +
                "\$331.50 —\$620.00 . . \$54.18 plus 24% —\$331.50 \$679.00 —\$1,256.00 . . \$108.38 plus 24% —\$679.00\n" +
                "\$620.00 —\$783.50 . . \$123.42 plus 32% —\$620.00 \$1,256.00 —\$1,582.90 . . \$246.86 plus 32% —\$1,256.00\n" +
                "\$783.50 —\$1,937.30 . . \$175.74 plus 35% —\$783.50 \$1,582.90 —\$2,352.10 . . \$351.47 plus 35% —\$1,582.90\n" +
                "\$1,937.30 ........... \$579.57 plus 37% —\$1,937.30 \$2,352.10 ........... \$620.69 plus 37% —\$2,352.10")

        brackets["Weekly_single"] = weekly[0]
        brackets["Weekly_married"] = weekly[1]
        brackets["Biweekly_single"] = biWeekly[0]
        brackets["Biweekly_married"] = biWeekly[1]
        brackets["Semimonthly_single"] = semiMonthly[0]
        brackets["Semimonthly_married"] = semiMonthly[1]
        brackets["Monthly_single"] = monthly[0]
        brackets["Monthly_married"] = monthly[1]
        brackets["Quarterly_single"] = quarterly[0]
        brackets["Quarterly_married"] = quarterly[1]
        brackets["Semiannual_single"] = semiAnnual[0]
        brackets["Semiannual_married"] = semiAnnual[1]
        brackets["Annual_single"] = annual[0]
        brackets["Annual_married"] = annual[1]
        brackets["Daily_single"] = daily[0]
        brackets["Daily_married"] = daily[1]

        brackets.forEach { println(it.toString()) }
    }
    */
}
