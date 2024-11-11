package com.example.couponverse

import android.content.Context
import android.content.SharedPreferences
import com.example.couponverse.data.User
import com.google.gson.Gson

/**
 * Manages application settings and user data using SharedPreferences.
 *
 * Provides methods to:
 * - Save and retrieve user settings.
 * - Save and retrieve application settings.
 * - Manage user password and login preferences.
 */
class PreferencesManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Saves the "Remember password?" preference.
     *
     * @param rememberMe Boolean indicating whether to remember the user or not.
     */
    fun setRememberMe(rememberMe: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", rememberMe)
        editor.apply()
    }

    /**
     * Retrieves the "Remember password?" preference.
     *
     * @return Boolean indicating whether the user is set to be remembered.
     */
    fun getRememberMe() : Boolean {
        return sharedPreferences.getBoolean("remember_me", false)
    }

    /**
     * Saves the current user settings to SharedPreferences as Json.
     *
     * @param newUserData The User object containing the user's data.
     */
    fun saveUserSettings(newUserData: User) {
        val editor = sharedPreferences.edit()
        val userJson = gson.toJson(newUserData)
        editor.putString("current_user", userJson)
        editor.apply()
    }

    /**
     * Saves the current user's raw (unhashed) password to SharedPreferences for automatic logins.
     *
     * @param pw The raw password of the user.
     */
    fun saveUserRawPassword(pw: String) {
        val editor = sharedPreferences.edit()
        editor.putString("current_user_password", pw)
        editor.apply()
    }

    /**
     * Retrieves the current user's raw (unhashed) password from SharedPreferences.
     *
     * @return The raw password of the user.
     */
    fun getUserRawPassword() : String {
        return sharedPreferences.getString("current_user_password", "")!!
    }

    /**
     * Saves application settings including the server URL and notification preference.
     *
     * @param changedUrl The URL of the server as a String.
     * @param notificationsEnabled Boolean indicating whether notifications are enabled.
     */
    fun saveAppSettings(changedUrl: String, notificationsEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putString("user_defined_url", changedUrl)
        editor.putBoolean("notifications_enabled", notificationsEnabled)
        editor.apply()
    }

    /**
     * Retrieves the current user's settings from SharedPreferences.
     *
     * @return The User object containing the user's data.
     */
    fun getUser() : User {
        val userJson = sharedPreferences.getString("current_user", gson.toJson(User(userName = "", password = "")))
        return gson.fromJson(userJson, User::class.java)
    }

    /**
     * Removes the current user's data from SharedPreferences.
     */
    fun removeUserData() {
        val editor = sharedPreferences.edit()
        editor.remove("current_user")
        editor.remove("current_user_password")
        editor.apply()
    }

    /**
     * Checks whether notifications are enabled.
     *
     * Currently not in use.
     *
     * @return Boolean indicating whether notifications are enabled.
     */
    fun isNotificationsOn() : Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", false)
    }

    /**
     * Sets the server URL in SharedPreferences.
     *
     * @param url The URL of the server as a String.
     */
    fun setServerUrl(url: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_defined_url", url)
        editor.apply()
    }

    /**
     * Retrieves the server URL from SharedPreferences.
     *
     * @return The URL of the server. Returns a default value if not set.
     */
    fun getServerUrl() : String {
        if (sharedPreferences.getString("user_defined_url", "").isNullOrEmpty())
            return "http://10.0.2.2:5073"
        return sharedPreferences.getString("user_defined_url", "")!!
    }
}