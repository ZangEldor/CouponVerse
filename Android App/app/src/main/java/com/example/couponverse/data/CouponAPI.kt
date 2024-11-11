package com.example.couponverse.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Data class representing a coupon to be sent to the server.
 * This class ensures compatibility between the server and the app by sending date fields as strings,
 * since the server and app use different date formats. All fields default to null and false.
 *
 * @property title The title of the coupon, typically a brief description of the offer.
 * @property company The name of the company offering the coupon.
 * @property category The category of the coupon.
 * @property expire_Date The expiration date of the coupon.
 * @property use_Date The date when the coupon was used, represented as a string.
 * @property is_Used A flag indicating whether the coupon has been used or not.
 * @property description A detailed description of the coupon or offer.
 * @property code The code of the coupon, which may be required for redeeming the offer.
 * @property original_Text The original text of the coupon, possibly from a scanned or parsed source.
 * @property bought_From The source where the coupon was bought or obtained.
 *
 * Implements the Serializable interface for easy passing between Android components.
 */
data class CouponAPI (
    var title: String? = null,
    var company: String? = null,
    var category: String? = null,
    var expire_Date: String? = null,
    var use_Date: String? = null,
    var is_Used: Boolean = false,
    var description: String? = null,
    var code: String? = null,
    var original_Text: String? = null,
    var bought_From: String? = null
) : Serializable {
    /**
     * Overrides the toString() method to provide a simplified string representation of the coupon,
     * useful for debugging.
     *
     * @return A string representation of the CouponAPI object.
     */
    override fun toString(): String {
        return "Coupon(Title = $title, Code = $code, Expire_date = ${expire_Date.toString()})"
    }

    /**
     * Converts this CouponAPI object into a `Coupon` object.
     *
     * @return A `Coupon` object created from the data in this `CouponAPI` object.
     * Requires API level 26 (Oreo) or higher due to the use of `LocalDate`.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun toCoupon() : Coupon {
        return Coupon(Title = title!!,
            Company = company,
            Category = category,
            Expire_Date = if (expire_Date.isNullOrEmpty()) null else
                LocalDate.parse(expire_Date, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            Is_Used = is_Used,
            Description = description,
            Code = code!!,
            Bought_From = bought_From)
    }

    /**
     * Updates the fields of this CouponAPI object based on the provided `Coupon` object.
     *
     * @param coupon The `Coupon` object whose data will be used to update this `CouponAPI`.
     * Requires API level 26 (Oreo) or higher due to the use of `LocalDate`.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun UpdateFieldsFromCoupon(coupon: Coupon) {
        this.title = coupon.Title
        this.company = coupon.Company
        this.category = coupon.Category
        this.expire_Date = if (coupon.Expire_Date == null) null else
            coupon.Expire_Date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        this.is_Used = coupon.Is_Used
        this.description = coupon.Description
        this.code = coupon.Code
        this.bought_From = coupon.Bought_From
    }
}