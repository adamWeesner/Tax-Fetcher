package weesner.tax_fetcher

import android.content.Context

/**
 * creates a new SocialSecurityObject
 *
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/20/2018
 *
 * @constructor creates a new SocialSecurityObject
 *
 * @property context needed to create the SocialSecurityObject
 * @property year the year to retrieve the fica tax information for
 */
class SocialSecurityTaxObject(context: Context, year: Int) : FicaTaxObject(context, SOCIAL_SECURITY, year)