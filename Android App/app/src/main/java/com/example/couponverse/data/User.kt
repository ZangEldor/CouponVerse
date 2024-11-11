package com.example.couponverse.data

import java.io.Serializable

/**
 * Data class representing a user in the application.
 *
 * @property id The unique identifier of the user. Optional, defaults to null.
 * @property userName The username of the user.
 * @property password The password of the user. Hashed for security.
 * @property salt A random value used for hashing the password. Optional, defaults to null.
 * @property picture An optional base64-encoded image representing the user's profile picture. Defaults to null.
 * @property averageEmbedding An optional list of doubles representing the user's average embedding for AI or recommendation purposes. Defaults to null.
 *
 * Implements the Serializable interface to allow passing of this object between Android components.
 */
data class User (
    var id: String? = null,
    var userName: String,
    var password: String,
    var salt: String? = null,
    var picture: String? = null,
    var averageEmbedding: List<Double>? = null
) : Serializable