# Tax-Fetcher
  A small library to fetch tax information for a given amount. Calculates Federal Income Tax, Medicare, Social Security, and Allowance cost for 2016, 2017 and 2018.

## Installation
  Add the gradle dependency to your Android project
  
  `implementation 'weesner.tax-fetcher:tax-fetcher:1.1.9'`  
  `implementation 'com.google.code.gson:gson:2.8.5'`

## Example Usage
Here is an example if your check was $400 for a week, you has $80 in healthcare deductions, your marital status is single, and no payroll allowances.
```kotlin
val federalTaxes = getFederalTaxes("2018").apply {
    // optional, for medicare and social security maxes
    yearToDateGross = 20000
    // the amount of the check you want to get taxes on
    checkAmount = 400
    // the federal income taxable amount; check amount minus any healthcare deductions       
    ficaTaxableAmount = 320
    // the marital status
    // "single" or "married"
    maritalStatus = SINGLE
    // the pay period length 
    // "Weekly","BiWeekly","Semimonthly","Quarterly","Semiannual","Annual" or "Daily"
    payPeriodType = WEEKLY
    // the amount of allowances from your W-2; 0-9 only
    payrollAllowances = 0
}
```

Getting the amounts the taxes will be for the given information above would look like this.
```kotlin
val medicareAmount = federalTaxes.medicare.amountOfCheck()
val socialSecurityAmount = federalTaxes.socialSecurity.amountOfCheck()
// needed to determine how much each payroll allowance is worth
val taxWithholding = federalTaxes.taxWithholding
val federalIncomeTax = federalTaxes.federalIncomeTax.apply {
    // adds the taxWithholding to the federalIncomeTax to calculate federal income tax correctly
    withholding = taxWithholding
}.amountOfCheck()
```  
