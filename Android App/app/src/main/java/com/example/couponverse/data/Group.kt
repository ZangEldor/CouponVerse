package com.example.couponverse.data

import java.io.Serializable

/**
 * Data class representing a community (group) in the application.
 *
 * @property id The unique identifier of the group. Optional, defaults to null.
 * @property name The name of the group, representing the community's title.
 * @property admins A list of user IDs or usernames representing the group admins.
 * @property users A list of user IDs or usernames representing the group's members.
 * @property messages A list of `Message` objects representing the messages sent within the group.
 * @property picture An optional base64-encoded image representing the group's picture. Defaults to null.
 *
 * Implements the Serializable interface to allow passing of this object between Android components.
 */
data class Group (
    var id: String? = null,
    var name: String,
    var admins: List<String>,
    var users: List<String>,
    var messages: List<Message>,
    var picture: String? = null
) : Serializable