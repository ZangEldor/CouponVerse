package com.example.couponverse.data

/**
 * Data class representing a product.
 *
 * @property imgUrl The URL of the product image.
 * @property title The name or title of the product.
 * @property productURL The URL to the product's page or where it can be purchased.
 * @property stars The product rating, represented as a floating-point number ( 0.0 to 5.0).
 * @property category_name The category to which the product belongs.
 * @property price The price of the product, represented as a double.
 * @property isBestSeller A boolean flag indicating whether the product is marked as a bestseller.
 */
data class Product (
    val imgUrl: String,
    val title: String,
    val productURL: String,
    val stars: Float,
    val category_name: String,
    val price: Double,
    val isBestSeller: Boolean
)
