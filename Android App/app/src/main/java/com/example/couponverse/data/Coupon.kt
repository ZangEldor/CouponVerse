package com.example.couponverse.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.LocalDate

/**
 * Data class representing a coupon, typically used in an application to manage discount offers or special deals.
 *
 * @property Title The title of the coupon, typically a brief description of the offer.
 * @property Company The name of the company offering the coupon. Optional, defaults to null.
 * @property Category The category of the coupon. Optional, defaults to null.
 * @property Expire_Date The expiration date of the coupon. Optional, defaults to null.
 * @property Is_Used A flag indicating whether the coupon has been used or not. Defaults to false.
 * @property Description A detailed description of the coupon or offer. Optional, defaults to null.
 * @property Code The code of the coupon, which may be required for redeeming the offer.
 * @property Bought_From The source where the coupon was bought or obtained. Optional, defaults to null.
 *
 * Implements the Serializable interface to allow the class to be easily passed between Android components.
 *
 * @constructor Initializes a new instance of the Coupon class with the provided properties.
 */
data class Coupon (
    var Title: String,
    var Company: String? = null,
    var Category: String? = null,
    var Expire_Date: LocalDate? = null,
    var Is_Used: Boolean,
    var Description: String? = null,
    var Code: String,
    var Bought_From: String? = null
) : Serializable {

    /**
     * Overrides the toString() method to provide a concise representation of the coupon, including
     * its title, code, and expiration date. Useful for debugging purposes.
     *
     * @return A string representation of the coupon object.
     */
    override fun toString(): String { // for debugging purposes
        return "Coupon(Title = $Title, Code = $Code, Expire_date = ${Expire_Date.toString()})"
    }

    /**
     * Fills the nullable fields of the coupon with default values if they are not provided.
     *
     * - Company: Defaults to an empty string if null.
     * - Category: Defaults to an empty string if null.
     * - Expire_Date: Defaults to the current date if null.
     * - Description: Defaults to an empty string if null.
     * - Bought_From: Defaults to an empty string if null.
     *
     * This method requires API level 26 (Oreo) or higher due to the use of LocalDate.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fillFields() {
        Company = Company ?: ""
        Category = Category ?: ""
        Expire_Date = Expire_Date ?: LocalDate.now()
        Description = Description ?: ""
        Bought_From = Bought_From ?: ""
    }
}
