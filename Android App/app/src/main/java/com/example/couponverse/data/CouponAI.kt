package com.example.couponverse.data

/**
 * Data class representing a coupon used in the return from NER. All fields default to null.
 *
 * @property title The title of the coupon, typically a brief description of the offer.
 * @property category The category of the coupon.
 * @property company The name of the company offering the coupon.
 * @property coupon_code The code of the coupon, which may be required for redeeming the offer.
 * @property description A detailed description of the coupon or offer.
 * @property expiration_date The expiration date of the coupon.
 */
data class CouponAI (
    var title: String? = null,
    var category: String? = null,
    var company: String? = null,
    var coupon_code: String? = null,
    var description: String? = null,
    var expiration_date: String? = null,
)
