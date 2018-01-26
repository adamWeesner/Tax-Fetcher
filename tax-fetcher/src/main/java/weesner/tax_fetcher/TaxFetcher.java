package weesner.tax_fetcher;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static weesner.tax_fetcher.Constants.ALLOWANCES;
import static weesner.tax_fetcher.Constants.FEDERAL_INCOME_TAX;
import static weesner.tax_fetcher.Constants.JSON;
import static weesner.tax_fetcher.Constants.MEDICARE;
import static weesner.tax_fetcher.Constants.NO_MORE_THAN;
import static weesner.tax_fetcher.Constants.PERCENT;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_ANNUAL;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_BIWEEKLY;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_DAILY;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_MONTHLY;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_QUARTERLY;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_SEMIANNUAL;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_SEMIMONTHLY;
import static weesner.tax_fetcher.Constants.PERIOD_TYPE_WEEKLY;
import static weesner.tax_fetcher.Constants.PLUS;
import static weesner.tax_fetcher.Constants.SOCIAL_SECURITY;
import static weesner.tax_fetcher.Constants.WITHHELD;


/**
 * Created by Adam Weesner on 10/3/2016.
 *
 * @deprecated in version 1.1.0 move to using FicaTaxObject, SocialSecurityTaxObject MedicareTaxObject FederalIncomeTaxObject to create items
 */
public class TaxFetcher {
    /**
     * @param context    used to retrieve resources from the assets folder
     * @param fileToLoad name of the JSON file to read from
     * @return the string that is to be passed into the JSONObject
     * A helper function to retrieve the JSON file from the assets folder
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static JSONObject loadJSONFromAsset(Context context, String fileToLoad) throws JSONException {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileToLoad)));
            String temp;
            while ((temp = br.readLine()) != null) sb.append(temp);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject(sb.toString());
    }

    /**
     * @param context used to retrieve resources from the assets folder
     * @param type    which file to load use MEDICARE or SOCIAL_SECURITY constants, or it will throw an error
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of the fica tax taken out of ones check
     * function to get Social Security and Medicare tax .json files
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getFica(Context context, String type, String year) {
        double ficaValue = 0;

        if (type.equals(MEDICARE) || type.equals(SOCIAL_SECURITY)) {
            try {
                JSONObject ficaObject = loadJSONFromAsset(context, type + JSON);
                JSONObject ficaItems = ficaObject.getJSONObject(type);
                ficaValue = ficaItems.getDouble(year);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException(String.format(context.getString(R.string.invalid_fica_type_error), MEDICARE, SOCIAL_SECURITY));
        }
        return ficaValue;
    }

    /**
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by medicare
     * helper function to get medicare.json using only year qualifier
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getMedicare(Context context, String year) {
        return getFica(context, MEDICARE, year);
    }

    /**
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by medicare
     * helper function to get medicare.json using only year qualifier
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getMedicare(Context context, int year) {
        return getMedicare(context, String.valueOf(year));
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param checkAmount the gross percent of ones check needed to figure out how much gets taxed
     * @param year        has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get the total percent that will be taken out of ones check for Medicare
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getMedicareTax(Context context, double checkAmount, String year) {
        return checkAmount * doubleToPercentage(getMedicare(context, year));
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param checkAmount the gross percent of ones check needed to figure out how much gets taxed
     * @param year        has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get the total percent that will be taken out of ones check for Medicare
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getMedicareTax(Context context, double checkAmount, int year) {
        return getMedicareTax(context, checkAmount, String.valueOf(year));
    }

    /**
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get socialSecurity.json using only year qualifier
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getSocialSecurity(Context context, String year) {
        return getFica(context, SOCIAL_SECURITY, year);
    }

    /**
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get socialSecurity.json using only year qualifier
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getSocialSecurity(Context context, int year) {
        return getSocialSecurity(context, String.valueOf(year));
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param checkAmount the gross percent of ones check needed to figure out how much gets taxed
     * @param year        has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get the total percent that will be taken out of ones check for Social Security
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getSocialSecurityTax(Context context, double checkAmount, String year) {
        return checkAmount * doubleToPercentage(getSocialSecurity(context, year));
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param checkAmount the gross percent of ones check needed to figure out how much gets taxed
     * @param year        has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     * helper function to get the total percent that will be taken out of ones check for Social Security
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getSocialSecurityTax(Context context, double checkAmount, int year) {
        return getSocialSecurityTax(context, checkAmount, String.valueOf(year));
    }

    /**
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be equal to one of the English constant provided starting with PERIOD_TYPE_
     * @param year       has to be 4 digit format eg: 2016
     * @return the cost of each allowance
     * function to get how much each allowance will cost based on periodType and year
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getAllowanceCost(Context context, String periodType, String year) {
        double allowanceCost = 0;
        if (periodType.equals(PERIOD_TYPE_WEEKLY) || periodType.equals(PERIOD_TYPE_BIWEEKLY) || periodType.equals(PERIOD_TYPE_SEMIMONTHLY) ||
                periodType.equals(PERIOD_TYPE_MONTHLY) || periodType.equals(PERIOD_TYPE_QUARTERLY) || periodType.equals(PERIOD_TYPE_SEMIANNUAL) ||
                periodType.equals(PERIOD_TYPE_ANNUAL) || periodType.equals(PERIOD_TYPE_DAILY)) {
            try {
                JSONObject allowancesObject = loadJSONFromAsset(context, ALLOWANCES + JSON);
                JSONObject allowanceObject = allowancesObject.getJSONObject(ALLOWANCES);
                JSONObject allowanceItems = allowanceObject.getJSONObject(year);
                allowanceCost = allowanceItems.getDouble(periodType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException(context.getString(R.string.invalid_allowance_type_error));
        }

        return allowanceCost;
    }

    /**
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be an English constant provided starting with PERIOD_TYPE_
     * @param year       has to be 4 digit format eg: 2016
     * @return the cost of each allowance
     * function to get how much each allowance will cost based on periodType and year
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getAllowanceCost(Context context, String periodType, int year) {
        return getAllowanceCost(context, periodType, String.valueOf(year));
    }

    /**
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances percent of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year       has to be 4 digit format eg: 2016
     * @return total cost of all allowances
     * function that gets the total cost of all ones allowances
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getTotalAllowancesCost(Context context, String periodType, int allowances, String year) {
        if (allowances <= 10) {
            return getAllowanceCost(context, periodType, year) * allowances;
        } else {
            throw new IllegalArgumentException(context.getString(R.string.invalid_allowance_quantity_error));
        }
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param periodType  needs to be an English constant provided starting with PERIOD_TYPE_
     * @param checkAmount the gross percent of ones check needed to figure out how much of it can be taxed
     * @param allowances  percent of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year        has to be 4 digit format eg: 2016
     * @return total cost of all allowances
     * function that gets the total percent of ones check that can be taxed
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getCanBeTaxedAmount(Context context, String periodType, double checkAmount, int allowances, String year) {
        return checkAmount - getTotalAllowancesCost(context, periodType, allowances, year);
    }

    /**
     * @param context     used to retrieve resources from the assets folder
     * @param periodType  needs to be an English constant provided starting with PERIOD_TYPE_
     * @param checkAmount the gross percent of ones check needed to figure out how much of it can be taxed
     * @param allowances  percent of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year        has to be 4 digit format eg: 2016
     * @return total cost of all allowances
     * function that gets the total percent of ones check that can be taxed
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getCanBeTaxedAmount(Context context, String periodType, double checkAmount, int allowances, int year) {
        return getCanBeTaxedAmount(context, periodType, checkAmount, allowances, String.valueOf(year));
    }

    /**
     * @param context       used to retrieve resources from the assets folder
     * @param checkAmount   the gross percent to figure out how much Federal Income Tax will be taken out
     * @param maritalStatus ones marital status needs to be equal to one of the constants starting with MARITAL_STATUS_
     * @param periodType    needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances    percent of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year          has to be 4 digit format eg: 2016
     * @return the percent of Federal Income Tax
     * function to get the percent of Federal Income Tax to be taken out of ones check
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, String year) {
        double fitCost = 0;
        double canBeTaxed = checkAmount - getTotalAllowancesCost(context, periodType, allowances, year);
        try {
            JSONObject federalIncomeTaxObject = loadJSONFromAsset(context, FEDERAL_INCOME_TAX + JSON);
            JSONObject fitItems = federalIncomeTaxObject.getJSONObject(FEDERAL_INCOME_TAX);
            JSONObject fitItemsForYear = fitItems.getJSONObject(year);
            JSONArray fitItemForTypeAndStatus = fitItemsForYear.getJSONArray(periodType + "_" + maritalStatus);
            boolean needValue = true;
            for (int i = 0; i < fitItemForTypeAndStatus.length(); i++) {
                try {
                    JSONObject fitQualifiers = fitItemForTypeAndStatus.getJSONObject(i);
                    if (canBeTaxed <= fitQualifiers.getDouble(NO_MORE_THAN)) {
                        if (needValue) {
                            needValue = false;
                            fitCost = federalIncomeTaxCost(canBeTaxed, fitQualifiers);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    if (needValue) {
                        needValue = false;
                        if (nfe.getMessage().equals(context.getString(R.string.invalid_double_error))) {
                            JSONObject fitQualifiers = fitItemForTypeAndStatus.getJSONObject(i);
                            fitCost = federalIncomeTaxCost(canBeTaxed, fitQualifiers);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fitCost;
    }

    /**
     * @param context       used to retrieve resources from the assets folder
     * @param checkAmount   the gross percent to figure out how much Federal Income Tax will be taken out
     * @param maritalStatus ones marital status needs to be equal to one of the constants starting with MARITAL_STATUS_
     * @param periodType    needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances    percent of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year          has to be 4 digit format eg: 2016
     * @return the percent of Federal Income Tax
     * function to get the percent of Federal Income Tax to be taken out of ones check
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    public static double getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, int year) {
        return getFederalIncomeTax(context, checkAmount, maritalStatus, periodType, allowances, String.valueOf(year));
    }

    /**
     * @param canBeTaxed the percent of ones check that can be taxed
     * @param qualifiers the JSONObject that contains the qualifiers for Federal Income Tax
     * @return the percent of Federal Income Tax to be taken out
     * @throws JSONException thrown if JSON information cannot be found
     *                       helper method to get the percent of Federal Income Tax to be taken out of ones check
     * @deprecated in version 1.1.0 move to using FederalIncomeTaxObject, FicaTaxObject, MedicareTaxObject, and SocialSecurityTaxObject
     */
    private static double federalIncomeTaxCost(double canBeTaxed, JSONObject qualifiers) throws JSONException {
        double plus = qualifiers.getDouble(PLUS);
        double percent = doubleToPercentage(qualifiers.getDouble(PERCENT));
        double withheld = qualifiers.getDouble(WITHHELD);
        return (canBeTaxed - withheld) * percent + plus;
    }

    /**
     * @param amount percent to convert to percentage
     * @return the percentage based on the double given
     * helper function to get the percentage of based on the double value given from the JSON file
     * @deprecated in version 1.1.0 move to using double.toPercentage()
     */
    public static double doubleToPercentage(double amount) {
        return amount / 100;
    }
}

