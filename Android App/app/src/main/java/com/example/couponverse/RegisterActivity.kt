package com.example.couponverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Activity for registering a new user and handling login after successful registration.
 *
 * Provides functionality to:
 * - Register a new user with server-side validation.
 * - Log in the user immediately after successful registration.
 * - Handle user credential storage and preferences.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var serverAPIManager: RetrofitAPIManager
    private lateinit var preferencesHelper: PreferencesManager

    /**
     * Initializes the activity, setting up the API manager, preferences helper, and UI elements.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        serverAPIManager = RetrofitAPIManager.getService(this)
        preferencesHelper = PreferencesManager(this)
    }

    /**
     * Starts the LoginActivity when called and finishes the current activity.
     *
     * @param view The view that triggered this method.
     */
    fun login(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    /**
     * Registers a new user with the server and handles the response.
     *
     * @param user The `User` object containing the registration details.
     */
    fun register(user: User) {
        val TAG = "CHECK_RESPONSE_REGISTER"
        serverAPIManager.postUserNoId(user, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // successful response
                if (response.isSuccessful) {
                    // when register succeeded, log in the user
                    loginAfterRegister(user)
                } else {
                    Log.i(TAG, "onResponse: unsuccessful - Code ${response.code()}}")
                    val regFailed = "Registration failed. Please try again."
                    Toast.makeText(this@RegisterActivity, regFailed, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(TAG, "onFailure: ${t.message}")
                val regFailed = "Registration failed. Please try again."
                Toast.makeText(this@RegisterActivity, regFailed, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Logs in the user after successful registration and handles preferences.
     *
     * @param user The `User` object with login details.
     */
    fun loginAfterRegister(user: User) {
        serverAPIManager.loginUser(user, object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) { // successful response
                    val result: User? = response.body()
                    val intent = Intent(applicationContext, MainMenuActivity::class.java)
                    intent.putExtra("userObject", result)
                    val pwRemember = findViewById<CheckBox>(R.id.rememberPwd)
                    preferencesHelper.setRememberMe(pwRemember.isChecked)
                    if (pwRemember.isChecked) {
                        // save user info and password (without hashing)
                        preferencesHelper.saveUserSettings(result!!)
                        preferencesHelper.saveUserRawPassword(user.password)
                    }
                    startActivity(intent)
                    finish()
                } else { // login failed
                    Log.i("CHECK_RESPONSE_LOG_AFTER_REG", "onResponse: unsuccessful - Code ${response.code()}}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.i("CHECK_RESPONSE_LOG_AFTER_REG", "onFailure: ${t.message}")
            }
        })
    }

    /**
     * Attempts to register a new user by validating input fields and checking if the user already exists.
     *
     * @param view The view that triggered this method.
     */
    fun attemptRegister(view: View) {
        val usr = findViewById<EditText>(R.id.UserNameReg).text.toString()
        val pw = findViewById<EditText>(R.id.PasswordReg).text.toString()
        val validate = findViewById<EditText>(R.id.PasswordConfirm).text.toString()

        if (usr.isEmpty() || pw.isEmpty() || validate.isEmpty()) {
            val emptyField = "Please fill all fields."
            Toast.makeText(this, emptyField, Toast.LENGTH_SHORT).show()
            return
        } else if (pw != validate) {
            val mismatch = "Passwords don't match. Please try again."
            Toast.makeText(this, mismatch, Toast.LENGTH_SHORT).show()
            return
        }

        var userExists: Boolean = false
        var errorOccurred: Boolean = false

        // check that the inserted user/mail is available
        serverAPIManager.checkUserExists(usr, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                userExists = !response.isSuccessful
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i("CHECK_RESPONSE_USERCHECK", "onFailure: ${t.message}")
                val error = "Request failed: ${t.message}"
                Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_SHORT).show()
                errorOccurred = true
            }
        })

        if(errorOccurred)
            return
        if (userExists) {
            val knownUser = "User already exists in our database."
            Toast.makeText(this@RegisterActivity, knownUser, Toast.LENGTH_SHORT).show()
            return
        }

        var newUser : User = User(userName = usr, password = pw)
        register(newUser)
    }
}