package com.example.couponverse

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.User


/**
 * Activity for displaying the main menu after user login.
 *
 * Provides functionality to:
 * - Display a welcome message with the user's details.
 * - Navigate to different sections of the app, such as communities, coupons, settings, and product browsing.
 */
class MainMenuActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var user: User

    /**
     * Initializes the activity, setting up UI elements and displaying user information.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        user = intent.extras!!.getSerializable("userObject") as User
        val username: String = (user.userName ?: "")
        findViewById<TextView>(R.id.MainNameDisplay).text = "Welcome, \n$username"

        val usrImgView: ImageView = findViewById<ImageView>(R.id.user_pfp)
        if (user.picture != null) {
            try {
                val decodedImage = Base64.decode(user.picture, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                usrImgView.setImageBitmap(imageBitmap)
            } catch (e: IllegalArgumentException) { // decoding fails
                usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
            }
        }
        else {
            usrImgView.setImageDrawable(ContextCompat.getDrawable(usrImgView.context, R.drawable.user_icon))
        }
    }

    /**
     * Navigates to the Communities section of the app.
     *
     * @param view The view that triggered this method.
     */
    fun gotoCommunities(view: View) {
        val intent = Intent(applicationContext, BrowseCommunitiesActivity::class.java)
        intent.putExtra("userObject", user)
        startActivity(intent)
    }

    /**
     * Navigates to the user's Coupons section.
     *
     * @param view The view that triggered this method.
     */
    fun gotoCouponsSelf(view: View) {
        val intent = Intent(applicationContext, BrowseCouponsActivity::class.java)
        intent.putExtra("userObject", user)
        startActivity(intent)
    }

    /**
     * Navigates to the Add Coupon screen.
     *
     * @param view The view that triggered this method.
     */
    fun gotoAddCoupon(view: View) {
        val intent = Intent(applicationContext, AddCouponActivity::class.java)
        intent.putExtra("userObject", user)
        intent.putExtra("operation", "add")
        startActivity(intent)
    }

    /**
     * Navigates to the App Settings screen.
     *
     * @param view The view that triggered this method.
     */
    fun gotoSettings(view: View) {
        val intent = Intent(applicationContext, AppSettingsActivity::class.java)
        intent.putExtra("userObject", user)
        startActivity(intent)
    }

    /**
     * Navigates to the Product Browsing section of the app.
     *
     * @param view The view that triggered this method.
     */
    fun gotoBrowseProducts(view: View) {
        val intent = Intent(applicationContext, MLProductsActivity::class.java)
        intent.putExtra("userObject", user)
        startActivity(intent)
    }

    /**
     * Navigates to the My Communities section of the app.
     *
     * @param view The view that triggered this method.
     */
    fun gotoMyCommunities(view: View) {
        val intent = Intent(applicationContext, UserCommunitiesActivity::class.java)
        intent.putExtra("userObject", user)
        startActivity(intent)
    }
}