package com.example.couponverse

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import com.example.couponverse.data.Coupon
import com.example.couponverse.data.CouponAPI
import com.example.couponverse.data.User
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.temporal.ChronoUnit

/**
 * Activity for browsing the user's list of coupons.
 *
 * Provides functionality to:
 * - Display and scroll through a user's coupons.
 * - Handle user interactions with coupon items (Edit/Delete a coupon on click).
 * - Sort the coupons by name or expiration date.
 * - Alerts when some coupons are about to expire
 * - Show status of each coupon based on expiration and usage.
 */
class BrowseCouponsActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var couponsContainer: LinearLayout
    private var currentUser: User? = null
    private var sortedByName: Boolean = false
    private var couponsList: List<Coupon>? = null
    private var couponsByName: List<Coupon>? = null
    private var couponsByDate: List<Coupon>? = null
    private val couponColorMap = mapOf(
        "Expired." to "#686868",
        "Used." to "#686868",
        "years" to "#64FF78",
        "year" to "#82FF82",
        "months" to "#A0FF64",
        "month" to "#D1E85B",
        "days" to "#FEC072",
        "Tomorrow" to "#FFAC88",
        "Today!" to "#FF7070"
    )

    /**
     * Initializes the activity, setting up UI elements and loading user coupons.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_browse_coupons)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.sort_setting).setImageResource(R.drawable.sort_by_date)
        couponsContainer = findViewById<LinearLayout>(R.id.couponsContainer)
        currentUser = intent.extras!!.getSerializable("userObject") as User

        val usrImgView: ImageView = findViewById<ImageView>(R.id.user_pfp)
        val user: User = currentUser as User // safe cast
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

        // get couponsList from the server
        serverAPIManager.getUserCouponsList(user.userName ?: "", object : Callback<List<CouponAPI>>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<CouponAPI>>, response: Response<List<CouponAPI>>) {
                if (response.isSuccessful) {
                    var couponAPIResponse = response.body()
                    couponsList = couponAPIResponse!!.map {it.toCoupon()}
                    manageDisplayCouponsList() // begin coupons' display upon receiving of the list
                } else {
                    Toast.makeText(this@BrowseCouponsActivity, "Failed to load coupons.", Toast.LENGTH_SHORT).show()
                    couponsList = null
                }
            }

            override fun onFailure(call: Call<List<CouponAPI>>, t: Throwable) {
                Toast.makeText(this@BrowseCouponsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                couponsList = null
            }
        })
    }

    /**
     * Manages the display of coupons by sorting and setting up the UI.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun manageDisplayCouponsList() {
        // if the list of coupons is not empty or null begin sorting them by Title/Expiration date
        var safeCastList: List<Coupon> = listOf()
        if (!couponsList.isNullOrEmpty()) {
            safeCastList = couponsList as List<Coupon>
            couponsByName = safeCastList.sortedBy { it.Title.lowercase() }
            couponsByDate = safeCastList.sortedBy { it.Expire_Date }
        }

        displayCoupons() // call function to display the coupons in the container

        // if no coupons or earliest coupon has over 7 days / already expired then remove alerts from display
        if (safeCastList.isNotEmpty()) { // if list is not empty
            for (c in safeCastList) {
                val daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), c.Expire_Date)

                if (daysUntilExpiration in 0..7) // if a coupon which is about to expire exists do nothing
                    return
            }
        }

        // no coupon which is about to expire detected - remove alert icons
        // hide alert icons
        val alertIconView = findViewById<ImageView>(R.id.alertImageView)
        val alertTextView = findViewById<TextView>(R.id.alertTextView)
        alertIconView.visibility = View.INVISIBLE
        alertTextView.visibility = View.INVISIBLE

        // enlarge coupons scroll view's height
        val couponsScrollView = findViewById<ScrollView>(R.id.scrollCouponsView)
        val viewParams = couponsScrollView.layoutParams
        viewParams.height = (590 * resources.displayMetrics.density).toInt()
        val viewConstraints = couponsScrollView.layoutParams as ConstraintLayout.LayoutParams
        viewConstraints.verticalBias = 0.64f
    }


    /**
     * Calculates the status of a coupon based on its expiration date and usage.
     *
     * @param coupon The coupon to check.
     * @return A string representing the status of the coupon.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcCouponStatus(coupon: Coupon) : String {
        if (coupon.Is_Used)
            return "Used."
        val period: Period = Period.between(LocalDate.now(), coupon.Expire_Date)

        return when {
            period.isNegative -> "Expired."
            period.years > 100 -> "Never"
            period.years > 5 -> "${period.years} years"
            period.years > 0 -> "One year"
            period.months > 1 -> "${period.months} months"
            period.months > 0 -> "One month"
            period.days > 1 -> "${period.days} days"
            period.days > 0 -> "Tomorrow"
            else -> "Today!"
        }
    }

    /**
     * Displays the coupons in the UI, sorted by the current sorting order.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayCoupons() {
        // select the corresponding list to the sorting option selected by the user
        if (couponsList.isNullOrEmpty()) {
            return
        }
        val couponsListDisplay: List<Coupon>? = if (sortedByName) couponsByName else couponsByDate
        if (couponsListDisplay.isNullOrEmpty()) { // if list is null/empty no need to do anything
            return
        }
        // remove all current views inside the container and begin display of coupons
        couponsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        var lastCouponViewDisplayed : View? = null
        for (coupon in couponsListDisplay) {
            // create new coupon view
            val couponView: View = inflater.inflate(R.layout.coupon_item, couponsContainer, false)
            // set margin between coupons
            (couponView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0 ,20)

            couponView.id = couponsList!!.indexOf(coupon)
            couponView.findViewById<TextView>(R.id.couponTitle).text = coupon.Title
            couponView.findViewById<TextView>(R.id.couponCode).text = coupon.Code
            val couponStatus = calcCouponStatus(coupon)
            couponView.findViewById<TextView>(R.id.couponExpiryStatus).text = couponStatus
            val dateString = coupon.Expire_Date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            couponView.findViewById<TextView>(R.id.couponExpiryDate).text = dateString

            // set new background color based on couponStatus
            val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.coupon_background)
            val layerDrawable = backgroundDrawable as LayerDrawable
            val shapeDrawable = layerDrawable.getDrawable(0) as GradientDrawable
            if (couponStatus == "Never") { // if coupon has "no expiration"
                couponView.findViewById<TextView>(R.id.couponExpiryDate).text = ""
                shapeDrawable.setColor(Color.parseColor("#8DCDFF"))
            } else {
                val statusLastWord = couponStatus.trim().split("\\s+".toRegex()).last()
                shapeDrawable.setColor(Color.parseColor(couponColorMap[statusLastWord]))
            }
            couponView.background = backgroundDrawable

            couponView.setOnClickListener { view ->
                showPopupMenu(view)
            }

            couponsContainer.addView(couponView)
            lastCouponViewDisplayed = couponView
        }
        // remove margins from last view
        if (lastCouponViewDisplayed == null)
                return
        (lastCouponViewDisplayed.layoutParams as LinearLayout.LayoutParams).setMargins(0)
    }

    /**
     * Toggles between sorting coupons alphabetically and by expiration date.
     *
     * @param view The view that triggered this method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleSortOption(view: View) {
        sortedByName = !sortedByName
        findViewById<ImageView>(R.id.sort_setting)
            .setImageResource(if (sortedByName) R.drawable.sort_alphabet
            else R.drawable.sort_by_date)
        displayCoupons() // re-display all coupons
    }

    /**
     * Shows a popup menu with options to edit or delete a coupon.
     *
     * @param view The view that triggered this method.
     */
    private fun showPopupMenu(view: View) {
        val couponTitle = view.findViewById<TextView>(R.id.couponTitle).text.toString()
        val couponCode = view.findViewById<TextView>(R.id.couponCode).text.toString()
        val couponIndex = getIndexOfCoupon(couponTitle, couponCode)
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.coupon_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_coupon -> { // start edit coupon activity
                    editCoupon(couponIndex)
                    true
                }
                R.id.delete_coupon -> { // delete coupon
                    deleteCoupon(couponIndex)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    /**
     * Starts the activity to edit a coupon.
     *
     * @param couponIndex The index of the coupon to be edited.
     */
    private fun editCoupon(couponIndex: Int) {
        if (couponIndex == -1) { // some kind of error
            Toast.makeText(this@BrowseCouponsActivity, "Failed editing coupon.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(applicationContext, AddCouponActivity::class.java)
        intent.putExtra("userObject", currentUser)
        intent.putExtra("couponObject", couponsList!![couponIndex])
        intent.putExtra("couponIndex", couponIndex)
        intent.putExtra("operation", "update")
        startActivity(intent)
    }

    /**
     * Refreshes the list of coupons from the server after a coupon has been edited.
     */
    private fun refreshCouponListFromServer() {
        // get couponsList
        val user: User = currentUser as User
        serverAPIManager.getUserCouponsList(user.userName ?: "", object : Callback<List<CouponAPI>>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<CouponAPI>>, response: Response<List<CouponAPI>>) {
                if (response.isSuccessful) {
                    var couponAPIResponse = response.body()
                    couponsList = couponAPIResponse!!.map {it.toCoupon()}
                    manageDisplayCouponsList() // begin coupons' display upon receiving of the list
                } else {
                    Toast.makeText(this@BrowseCouponsActivity, "Failed to load coupons.", Toast.LENGTH_SHORT).show()
                    couponsList = null
                }
            }

            override fun onFailure(call: Call<List<CouponAPI>>, t: Throwable) {
                Toast.makeText(this@BrowseCouponsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                couponsList = null
            }
        })
    }

    /**
     * Deletes a coupon from the list.
     *
     * @param couponIndex The index of the coupon to be deleted.
     */
    private fun deleteCoupon(couponIndex: Int) {
        if (couponIndex == -1) { // some kind of error
            Toast.makeText(this@BrowseCouponsActivity, "Failed deleting coupon.", Toast.LENGTH_SHORT).show()
            return
        }
        attemptDeleteCoupon(currentUser!!.userName, couponIndex)
    }

    /**
     * Attempts to delete a coupon via the server API.
     *
     * @param username The username of the current user.
     * @param index The index of the coupon to be deleted.
     */
    private fun attemptDeleteCoupon(username: String, index: Int) {
        serverAPIManager.deleteCoupon(username, index, object : Callback<Void>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    refreshCouponList(index)
                } else {
                    Toast.makeText(this@BrowseCouponsActivity, "Failed to delete coupon.", Toast.LENGTH_SHORT).show()
                    couponsList = null
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BrowseCouponsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                couponsList = null
            }
        })
    }

    /**
     * Refreshes the coupon list after a coupon is deleted.
     *
     * @param index The index of the deleted coupon.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshCouponList(index: Int){
        // remove coupon at index
        var safeCastList: List<Coupon> = couponsList as List<Coupon>
        val mutableCouponList: MutableList<Coupon> = safeCastList.toMutableList()
        mutableCouponList.removeAt(index)

        // swap old lists with new
        safeCastList = mutableCouponList.toList() as List<Coupon>
        couponsList = safeCastList
        if (safeCastList.isEmpty()) {
            couponsByName = safeCastList
            couponsByDate = safeCastList
        } else {
            couponsByName = safeCastList.sortedBy { it.Title.lowercase() }
            couponsByDate = safeCastList.sortedBy { it.Expire_Date }
        }

        displayCoupons() // redisplay coupons
    }

    /**
     * Finds the index of a coupon based on its title and code.
     *
     * @param title The title of the coupon.
     * @param code The code of the coupon.
     * @return The index of the coupon, or -1 if not found.
     */
    private fun getIndexOfCoupon(title: String, code: String) : Int {
        if (couponsList == null)
            return -1
        val safeCastCouponsList: List<Coupon> = couponsList as List<Coupon>
        return safeCastCouponsList.indexOfFirst { it.Title == title && it.Code == code }
    }
}