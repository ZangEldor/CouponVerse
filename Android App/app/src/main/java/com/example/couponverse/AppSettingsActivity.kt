package com.example.couponverse


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import android.widget.Button
import com.example.couponverse.data.UserUpdateData

/**
 * Activity for managing app settings and user profile updates.
 *
 * Provides functionality to:
 * - Select and update the user's profile picture.
 * - Update user settings (username, profile picture).
 * - Update app settings (notifications, server URL).
 * - Log out of the app.
 */
class AppSettingsActivity : AppCompatActivity() {

    private lateinit var preferencesHelper: PreferencesManager
    private lateinit var userPicture: ImageView
    private lateinit var serverAPIManager: RetrofitAPIManager
    private lateinit var currentUser: User
    private val MAX_IMAGE_WIDTH_HEIGHT = 256

    /**
     * Initializes the activity, setting up UI elements and loading current settings.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setBackgroundColor(Color.RED) // Ensure this line is being executed
        btnLogout.setTextColor(Color.WHITE) // Ensure the text color is set for visibility

        preferencesHelper = PreferencesManager(this)
        userPicture = findViewById<ImageView>(R.id.picture_view)
        serverAPIManager = RetrofitAPIManager.getService(this)
        currentUser = intent.extras!!.getSerializable("userObject") as User

        // load current settings
        // user settings
        if (!currentUser.userName.isNullOrEmpty())
            findViewById<EditText>(R.id.username_edit).setText(currentUser.userName)

        if (!currentUser.picture.isNullOrEmpty()) {
            try {
                val decodedImage = Base64.decode(currentUser.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                userPicture.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                userPicture.setImageDrawable(ContextCompat.getDrawable(userPicture.context, R.drawable.user_icon))
            }
        }
        else {
            userPicture.setImageDrawable(ContextCompat.getDrawable(userPicture.context, R.drawable.user_icon))
        }
        // app settings
        findViewById<Switch>(R.id.notifications_switch).isChecked = preferencesHelper.isNotificationsOn()
        findViewById<EditText>(R.id.server_url_edit).setText(preferencesHelper.getServerUrl())
    }

    /**
     * Opens the image picker for selecting a new profile picture.
     *
     * @param view The view that triggered this method.
     */
    fun selectImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 3)
    }

    /**
     * Handles the result of the image picker, updating the profile picture if a new image was selected.
     *
     * @param requestCode The request code passed in `startActivityForResult`.
     * @param resultCode The result code returned by the child activity.
     * @param data The data returned by the child activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImage: Uri? = data.data
            userPicture.setImageURI(selectedImage)
            resizeImage()
        }
    }

    /**
     * Resizes the selected profile picture to fit within a predefined dimension square while
     * maintaining aspect ratio.
     */
    private fun resizeImage() {
        val drawable = userPicture.drawable ?: return
        val bitmap = (drawable as BitmapDrawable).bitmap

        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        var newWidth = MAX_IMAGE_WIDTH_HEIGHT
        var newHeight = MAX_IMAGE_WIDTH_HEIGHT
        if (width > height) {
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newWidth = (newHeight * aspectRatio).toInt()
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        userPicture.setImageBitmap(resizedBitmap)
    }

    /**
     * Updates the user profile with new settings.
     *
     * @param view The view that triggered this method.
     */
    fun updateUser(view: View) {
        val username = findViewById<EditText>(R.id.username_edit).text.toString()
        if (username.isNullOrEmpty()) {
            val noUsername = "Please select a username."
            Toast.makeText(this@AppSettingsActivity, noUsername, Toast.LENGTH_SHORT).show()
            return
        }
        val pictureBase64 = getUserImageBase64()
        // check if the new username proposed already exists
        var userExists: Boolean = false
        var errorOccurred: Boolean = false
        serverAPIManager.checkUserExists(username, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                userExists = response.isSuccessful
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i("CHECK_RESPONSE_USERCHECK", "onFailure: ${t.message}")
                val error = "Request failed: ${t.message}"
                Toast.makeText(this@AppSettingsActivity, error, Toast.LENGTH_SHORT).show()
                errorOccurred = true
            }
        })
        if (errorOccurred)
            return
        if (userExists && username != currentUser.userName) {
            val knownUser = "Username unavailable."
            Toast.makeText(this@AppSettingsActivity, knownUser, Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser: User = currentUser.copy()
        updatedUser.userName = username
        updatedUser.picture = pictureBase64

        val updateUserData: UserUpdateData = UserUpdateData(
            userName = username,
            password = updatedUser.password,
            picture = pictureBase64)

        // update the user
        updateUserOnServer(updateUserData, updatedUser) // update on server
    }

    /**
     * Sends a request to update user information on the server.
     *
     * @param userData The updated user data.
     * @param updatedUser The updated user object.
     */
    private fun updateUserOnServer(userData: UserUpdateData, updatedUser: User, ) {
        val TAG = "CHECK_RESPONSE_USERUPDATE"
        serverAPIManager.updateUserByName(currentUser.userName, userData, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // successful response
                if  (response.isSuccessful) { // update locally and go to main menu
                    preferencesHelper.saveUserSettings(updatedUser)
                    currentUser = updatedUser
                    returnToMainMenu()
                }
                else { // update failed
                    Log.i(TAG, "onResponse: unsuccessful - Code ${response.code()}}")
                    val updateFailed = "Failed updating user. Please try again."
                    Toast.makeText(this@AppSettingsActivity, updateFailed, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(TAG, "onFailure: ${t.message}")
                val updateFailed = "Failed updating user. Please try again."
                Toast.makeText(this@AppSettingsActivity, updateFailed, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Converts the user's profile picture to a Base64-encoded string.
     *
     * @return The Base64-encoded string representation of the image.
     */
    private fun getUserImageBase64() : String? {
        val drawable = userPicture.drawable ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        // convert the Bitmap to a Base64-encoded string
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Updates the app settings locally and saves them.
     *
     * @param view The view that triggered this method.
     */
    fun updateApp(view: View) {
        val notifications = findViewById<Switch>(R.id.notifications_switch).isChecked
        val serverUrl = findViewById<EditText>(R.id.server_url_edit).getText().toString()
        preferencesHelper.saveAppSettings(serverUrl, notifications)
    }

    /**
     * Logs out the user and returns to the login screen.
     *
     * @param view The view that triggered this method.
     */
    fun logout(view: View) {
        preferencesHelper.setRememberMe(false)
        preferencesHelper.removeUserData()
        val intent = Intent(this, LoginActivity::class.java)

        // Set flags to clear the stack and start the activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    /**
     * Navigates back to the main menu and finishes all current activities.
     */
    private fun returnToMainMenu() {
        // notify the user of update's success
        val updateSuccessful = "User updated successfully!"
        Toast.makeText(this@AppSettingsActivity, updateSuccessful, Toast.LENGTH_SHORT).show()
        // "restart" the app - go to main menu but finish all tasks before it
        val intent = Intent(applicationContext, MainMenuActivity::class.java)
        intent.putExtra("userObject", preferencesHelper.getUser())
        startActivity(intent)
    }
}