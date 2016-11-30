# Tax-Fetcher
  A small library to fetch tax information for a given amount. Calculates Federal Income Tax, Medicare, Social Security, and Allowance cost for 2015 and 2016.

## Installation
  Add the gradle dependency to your Android project
  
  `compile 'weesner:tax-fetcher:1.0.1'`

## Example Usage
  How much Federal Income Tax will I get taken out of my weekly check that is $400?
  
  Adding the below line of code will give you how much Federal Income Tax will be taken out of your check with 0 Allowances
  `TaxFetcher.getFederalIncomeTax(this, 400, TaxFetcher.MARITAL_STATUS_SINGLE, TaxFetcher.PERIOD_TYPE_WEEKLY, 0, 2016);`
  
  The function definition is:
  `public static double getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, int year)`
  
  All of the functions are very similar, requiring only the minimal needed information to properly calculate the amount.
  The main functions provided are as follows all returning `double`'s:
  `TaxFetcher.getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, int year)`
  `TaxFetcher.getMedicare(Context context, int year)`
  `TaxFetcher.getSocialSecurity(Context context, int year)`
  `TaxFetcher.getTotalAllowanceCost(Context context, String periodType, int allowances, int year)`
   
  There are several others so check out the code to see the others and the behind the scenes functions. They are super simple to use.
  

