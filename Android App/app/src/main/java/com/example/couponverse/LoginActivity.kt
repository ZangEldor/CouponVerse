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
 * Activity for handling user login and registration.
 *
 * Provides functionality to:
 * - Log in users with their credentials.
 * - Navigate to the registration screen upon register selection.
 * - Automatically log in users if they have been remembered.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var serverAPIManager: RetrofitAPIManager
    private lateinit var preferencesHelper: PreferencesManager

    /**
     * Initializes the activity, setting up UI elements and checking if a user is already logged in.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        serverAPIManager = RetrofitAPIManager.getService(this)
        preferencesHelper = PreferencesManager(this)

        checkIfUserAlreadyLoggedIn()
    }

    /**
     * Checks if a user is already logged in and attempts to auto-login if applicable.
     */
    private fun checkIfUserAlreadyLoggedIn() {
        val user: User = preferencesHelper.getUser()
        val password: String = preferencesHelper.getUserRawPassword()
        // if no username or password return
        if (user.userName.isEmpty() || password.isEmpty()) {
            return
        }
        // if user didn't wish to be automatically logged in
        if (!preferencesHelper.getRememberMe()) {
            return
        }
        // attempt login with saved info
        val loginInfo: User = User(userName = user.userName, password = password)
        serverAPIManager.loginUser(loginInfo, object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) { // successful login
                    val result: User? = response.body()
                    val intent = Intent(applicationContext, MainMenuActivity::class.java)
                    intent.putExtra("userObject", user)
                    startActivity(intent)
                    finish()
                }
                else { // login failed
                    val loginFailedMsg = "Incorrect login detail, automatic login cancelled."
                    Toast.makeText(this@LoginActivity, loginFailedMsg, Toast.LENGTH_SHORT).show()

                    // clear remembered user data
                    preferencesHelper.removeUserData()
                    preferencesHelper.setRememberMe(false)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) { // call failed
                // clear remembered user data
                preferencesHelper.removeUserData()
                preferencesHelper.setRememberMe(false)
                Log.i("CHECK_RESPONSE_AUTO_LOGIN", "onFailure: ${t.message}")
                Toast.makeText(this@LoginActivity, "Auto-login failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Navigates to the registration activity when the register button is clicked.
     *
     * @param view The view that triggered this method.
     */
    fun register(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    /**
     * Logs in the user with the provided credentials and handles the response.
     *
     * @param user The user object containing login credentials.
     * @param callback The callback for handling the server response.
     */
    private fun loginWithUserInfo(user: User, callback: RESTfulApiResponseCallback<User?>) {
        val tag = "CHECK_RESPONSE_LOGIN"
        serverAPIManager.loginUser(user, object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) { // successful response
                    val result: User? = response.body()
                    callback.onSuccess(result)
                } else { // login failed
                    Log.i(tag, "onResponse: unsuccessful - Code ${response.code()}}")
                    callback.onFailure("Incorrect login detail, please try again.")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.i(tag, "onFailure: ${t.message}")
                callback.onFailure("Request failed: ${t.message}")
            }
        })
    }

    /**
     * Handles user login when the login button is clicked.
     *
     * @param view The view that triggered this method.
     */
    fun login(view: View) {
        val usr = findViewById<EditText>(R.id.UserNameLogin).text.toString()
        val pw = findViewById<EditText>(R.id.PasswordLogin).text.toString()

        if (usr.isEmpty() || pw.isEmpty()) {
            val emptyField = "Please fill all fields."
            Toast.makeText(this, emptyField, Toast.LENGTH_SHORT).show()
            return
        }

        val user: User = User(userName = usr, password = pw)

        loginWithUserInfo(user, object : RESTfulApiResponseCallback<User?> {
            override fun onSuccess(result: User?) {
                if (result == null) {
                    val unknownUser = "User doesn't exist in our database."
                    Toast.makeText(this@LoginActivity, unknownUser, Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(applicationContext, MainMenuActivity::class.java)
                intent.putExtra("userObject", result)
                val pwRemember = findViewById<CheckBox>(R.id.rememberPwd)
                preferencesHelper.setRememberMe(pwRemember.isChecked)
                if (pwRemember.isChecked) {
                    // save user info and password (without hashing)
                    preferencesHelper.saveUserSettings(result)
                    preferencesHelper.saveUserRawPassword(pw)
                }
                startActivity(intent)
                finish()
            }

            override fun onFailure(error: String) {
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
            }
        })
    }
}