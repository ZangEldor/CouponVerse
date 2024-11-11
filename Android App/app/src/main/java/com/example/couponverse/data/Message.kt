package com.example.couponverse.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data class representing a message in a group or community.
 *
 * @property sender The ID or username of the user who sent the message.
 * @property data The content of the message, typically a text string.
 * @property timestamp_string The date when the message was sent, represented as a text string.
 * @property timestamp The date when the message was sent, represented as a `LocalDateTime`.
 *
 * Implements the Serializable interface to allow passing of this object between Android components.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Message (
    var sender: String,
    var data: String,
    var timestamp_string: String
): Serializable {
    val timestamp: LocalDate = LocalDate.parse(timestamp_string, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}



