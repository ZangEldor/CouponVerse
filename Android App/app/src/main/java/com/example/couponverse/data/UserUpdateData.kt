package com.example.couponverse.data

import java.io.Serializable

/**
 * Data class representing the data required for updating a user's profile information.
 *
 * @property userName The new username to set for the user.
 * @property password The new password for the user. Typically hashed for security.
 * @property picture An optional base64-encoded image representing the user's updated profile picture. Defaults to null.
 *
 * Implements the Serializable interface to allow passing of this object between Android components.
 */
data class UserUpdateData (
    var userName: String,
    var password: String,
    var picture: String?
) : Serializable
