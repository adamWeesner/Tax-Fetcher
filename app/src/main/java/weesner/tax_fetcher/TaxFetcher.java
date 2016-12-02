package weesner.tax_fetcher;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by alwee on 10/3/2016.
 */

public class TaxFetcher {
    // the file names in assets folder to be used in retrieving values
    public static final String MEDICARE = "medicare";
    public static final String SOCIAL_SECURITY = "socialSecurity";
    public static final String ALLOWANCES = "allowances";
    public static final String FEDERAL_INCOME_TAX = "federalIncomeTax";
    // English constants for retrieving allowances
    public static final String PERIOD_TYPE_WEEKLY = "Weekly";
    public static final String PERIOD_TYPE_BIWEEKLY = "Biweekly";
    public static final String PERIOD_TYPE_SEMIMONTHLY = "Semimonthly";
    public static final String PERIOD_TYPE_MONTHLY = "Monthly";
    public static final String PERIOD_TYPE_QUARTERLY = "Quarterly";
    public static final String PERIOD_TYPE_SEMIANNUAL = "Semiannual";
    public static final String PERIOD_TYPE_ANNUAL = "Annual";
    public static final String PERIOD_TYPE_DAILY = "Daily";
    // English constants for married and single
    public static final String MARITAL_STATUS_SINGLE = "Single";
    public static final String MARITAL_STATUS_MARRIED = "Married";

    /**
     * A helper function to retrieve the JSON file from the assets folder
     *
     * @param context    used to retrieve resources from the assets folder
     * @param fileToLoad name of the JSON file to read from
     * @return the string that is to be passed into the JSONObject
     */
    public static JSONObject loadJSONFromAsset(Context context, String fileToLoad) throws JSONException {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileToLoad)));
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject(sb.toString());
    }

    /**
     * function to get Social Security and Medicare tax .json files
     *
     * @param context used to retrieve resources from the assets folder
     * @param type    which file to load use MEDICARE or SOCIAL_SECURITY constants, or it will throw an error
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of the fica tax taken out of ones check
     */
    public static double getFica(Context context, String type, String year) {
        double ficaValue = 0;
        if (type.equals(MEDICARE) || type.equals(SOCIAL_SECURITY)) {
            try {
                JSONObject ficaObject = loadJSONFromAsset(context, type + ".json");
                JSONObject ficaItems = ficaObject.getJSONObject(type);
                ficaValue = ficaItems.getDouble(year);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Invalid type. Type can only be: " + MEDICARE + " or " + SOCIAL_SECURITY);
        }
        return ficaValue;
    }

    /**
     * helper function to get medicare.json using only year qualifier
     *
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by medicare
     */
    public static double getMedicare(Context context, String year) {
        return getFica(context, MEDICARE, year);
    }

    /**
     * helper function to get medicare.json using only year qualifier
     *
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by medicare
     */
    public static double getMedicare(Context context, int year) {
        return getMedicare(context, String.valueOf(year));
    }

    public static double getMedicareTax(Context context, double checkAmount, String year) {
        return checkAmount * (getMedicare(context, year) / 100);
    }

    public static double getMedicareTax(Context context, double checkAmount, int year) {
        return getMedicareTax(context, checkAmount, String.valueOf(year));
    }

    /**
     * helper function to get socialSecurity.json using only year qualifier
     *
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     */
    public static double getSocialSecurity(Context context, String year) {
        return getFica(context, SOCIAL_SECURITY, year);
    }

    /**
     * helper function to get socialSecurity.json using only year qualifier
     *
     * @param context used to retrieve resources from the assets folder
     * @param year    has to be 4 digit format eg: 2016
     * @return the percentage of ones check that is taken out by social security
     */
    public static double getSocialSecurity(Context context, int year) {
        return getSocialSecurity(context, String.valueOf(year));
    }

    public static double getSocialSecurityTax(Context context, double checkAmount, String year) {
        return checkAmount * (getSocialSecurity(context, year) / 100);
    }

    public static double getSocialSecurityTax(Context context, double checkAmount, int year) {
        return getSocialSecurityTax(context, checkAmount, String.valueOf(year));
    }

    /**
     * function to get how much each allowance will cost based on periodType and year
     *
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be equal to one of the English constant provided starting with PERIOD_TYPE_
     * @param year       has to be 4 digit format eg: 2016
     * @return the cost of each allowance
     */
    public static double getAllowanceCost(Context context, String periodType, String year) {
        double allowanceCost = 0;
        if (periodType.equals(PERIOD_TYPE_WEEKLY) || periodType.equals(PERIOD_TYPE_BIWEEKLY) || periodType.equals(PERIOD_TYPE_SEMIMONTHLY) ||
                periodType.equals(PERIOD_TYPE_MONTHLY) || periodType.equals(PERIOD_TYPE_QUARTERLY) || periodType.equals(PERIOD_TYPE_SEMIANNUAL) ||
                periodType.equals(PERIOD_TYPE_ANNUAL) || periodType.equals(PERIOD_TYPE_DAILY)) {
            try {
                JSONObject allowancesObject = loadJSONFromAsset(context, ALLOWANCES + ".json");
                JSONObject allowanceObject = allowancesObject.getJSONObject(ALLOWANCES);
                JSONObject allowanceItems = allowanceObject.getJSONObject(year);
                allowanceCost = allowanceItems.getDouble(periodType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Period Type must be equal to one of the English constants provided starting with PERIOD_TYPE_ eg: PERIOD_TYPE_WEEKLY");
        }

        return allowanceCost;
    }

    /**
     * function to get how much each allowance will cost based on periodType and year
     *
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be an English constant provided starting with PERIOD_TYPE_
     * @param year       has to be 4 digit format eg: 2016
     * @return the cost of each allowance
     */
    public static double getAllowanceCost(Context context, String periodType, int year) {
        return getAllowanceCost(context, periodType, String.valueOf(year));
    }

    /**
     * function that gets the total cost of all ones allowances
     *
     * @param context    used to retrieve resources from the assets folder
     * @param periodType needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances amount of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year       has to be 4 digit format eg: 2016
     * @return total cost of all allowances
     */
    public static double getTotalAllowancesCost(Context context, String periodType, int allowances, String year) {
        if (allowances <= 10) {
            return getAllowanceCost(context, periodType, year) * allowances;
        } else {
            throw new IllegalArgumentException("The amount of allowances cannot exceed 10");
        }
    }

    public static double getCanBeTaxedAmount(Context context, String periodType, double checkAmount, int allowances, String year) {
        return checkAmount - getTotalAllowancesCost(context, periodType, allowances, year);
    }

    public static double getCanBeTaxedAmount(Context context, String periodType, double checkAmount, int allowances, int year) {
        return getCanBeTaxedAmount(context, periodType, checkAmount, allowances, String.valueOf(year));
    }

    /**
     * function to get the amount of Federal Income Tax to be taken out of ones check
     *
     * @param context       used to retrieve resources from the assets folder
     * @param checkAmount   the gross amount to figure out how much Federal Income Tax will be taken out
     * @param maritalStatus ones marital status needs to be equal to one of the constants starting with MARITAL_STATUS_
     * @param periodType    needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances    amount of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year          has to be 4 digit format eg: 2016
     * @return the amount of Federal Income Tax
     */
    public static double getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, String year) {
        double fitCost = 0;
        double canBeTaxed = checkAmount - getTotalAllowancesCost(context, periodType, allowances, year);
        try {
            JSONObject federalIncomeTaxObject = loadJSONFromAsset(context, FEDERAL_INCOME_TAX + ".json");
            JSONObject fitItems = federalIncomeTaxObject.getJSONObject(FEDERAL_INCOME_TAX);
            JSONObject fitItemsForYear = fitItems.getJSONObject(year);
            JSONArray fitItemForTypeAndStatus = fitItemsForYear.getJSONArray(periodType + "_" + maritalStatus);
            boolean needValue = true;
            for (int k = 0; k < fitItemForTypeAndStatus.length(); k++) {
                try {
                    JSONObject fitQualifiers = fitItemForTypeAndStatus.getJSONObject(k);
                    if (canBeTaxed <= stringToDouble(fitQualifiers.getString("noMoreThan"))) {
                        if (needValue) {
                            needValue = false;
                            double plus = fitQualifiers.getDouble("plus");
                            double percent = fitQualifiers.getDouble("percent") / 100;
                            double withheld = fitQualifiers.getDouble("withheld");
                            fitCost = (canBeTaxed - withheld) * percent + plus;
                        }
                    }
                } catch (NumberFormatException nfe) {
                    if (needValue) {
                        needValue = false;
                        if (nfe.getMessage().equals("Invalid double: \"max\"")) {
                            JSONObject fitQualifiers = fitItemForTypeAndStatus.getJSONObject(k);
                            double plus = fitQualifiers.getDouble("plus");
                            double percent = fitQualifiers.getDouble("percent") / 100;
                            double withheld = fitQualifiers.getDouble("withheld");
                            fitCost = (canBeTaxed - withheld) * percent + plus;
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
     * function to get the amount of Federal Income Tax to be taken out of ones check
     *
     * @param context       used to retrieve resources from the assets folder
     * @param checkAmount   the gross amount to figure out how much Federal Income Tax will be taken out
     * @param maritalStatus ones marital status needs to be equal to one of the constants starting with MARITAL_STATUS_
     * @param periodType    needs to be an English constant provided starting with PERIOD_TYPE_
     * @param allowances    amount of allowances one has entered, can be 0 - 10 any other number will throw error
     * @param year          has to be 4 digit format eg: 2016
     * @return the amount of Federal Income Tax
     */
    public static double getFederalIncomeTax(Context context, double checkAmount, String maritalStatus, String periodType, int allowances, int year) {
        return getFederalIncomeTax(context, checkAmount, maritalStatus, periodType, allowances, String.valueOf(year));
    }


    private static double stringToDouble(String value) {
        return Double.parseDouble(value);
    }
}

