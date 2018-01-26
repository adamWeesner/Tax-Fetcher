package weesner.tax_fetcher

import android.content.Context

/**
 * creates a new MedicareTaxObject
 *
 * Created for Weesner Development
 * @author Adam Weesner
 * @since 1/20/2018
 *
 * @constructor creates a new MedicareTaxObject
 *
 * @property context needed to create the MedicareTaxObject
 * @property year the year to retrieve the fica tax information for
 */
class MedicareTaxObject(context: Context, year: Int) : FicaTaxObject(context, MEDICARE, year)