package com.example.couponverse.data

import java.io.Serializable


/**
 * Data class representing the data required for updating a user's username.
 *
 * @property oldUsername The current username of the user that needs to be updated.
 * @property newUsername The new username that the user wants to set.
 *
 * Implements the Serializable interface to allow passing of this object between Android components.
 */
data class UsernameUpdateData (
    var oldUsername: String,
    var newUsername: String
) : Serializable
