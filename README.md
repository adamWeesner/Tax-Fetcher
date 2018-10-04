# Tax-Fetcher
  A small library to fetch tax information for a given amount. Calculates Federal Income Tax, Medicare, Social Security, and Allowance cost for 2016, 2017 and 2018.

## Installation
  Add the gradle dependency to your Android project
  
  `implementation 'weesner.tax-fetcher:tax-fetcher:1.2.3'`
  `implementation 'com.google.code.gson:gson:2.8.5'`

## Example Usage
Here is an example if your check was $400 for a week, you has $80 in healthcare deductions, 10% of your gross check amount goes to your retirement, your marital status is single, and 1 payroll allowance.
```kotlin
// the pay information for the check, this example just passes in the $400 gross check amount
val payInfo = PayInfo(400.0)
// the retirement that is 10 percent of your check before taxes
val retirement = Retirement("My retirement fund", 10.0, true, true)
// the $80 healthcare deduction being taken out of your check
val deduction = PayrollDeduction(name = "my deduction", amount = 80, isPercentage = false, isHealthCare = true)
// the check object, passing in all of the values from above
val check = Check(payInfo, PayrollInfo(), retirement, deduction)
// create a federal tax object for 2018
val federalTaxes = getFederalTaxes(taxesForYear = 2018)
```

Calculating the taxes for the above information would look like this.
```kotlin
check.calculateTaxes(federalTaxes!!)

println("medicare: ${check.medicareAmount}")
println("social security: ${check.socialSecurityAmount}")
println("federal income tax: ${check.federalIncomeTaxAmount}")
println("federal taxes total: ${check.federalTaxesAmount}")

println("deductions amount: ${check.deductionsAmount}")
println("retirement amount: ${check.retirementAmount}")

println("after taxes and deductions: ${check.afterTax}")

```