package com.example.couponverse

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.Coupon
import com.example.couponverse.data.CouponAPI
import com.example.couponverse.data.User
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Activity for editing a coupon's details.
 *
 * Provides functionality to:
 * - Edit an existing coupon's data.
 * - Save changes to the coupon on the server and locally.
 * - Update the user's embeddings.
 */
class EditCouponActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var currentUser: User
    private lateinit var currentCoupon: Coupon
    private var index: Int = -1
    private lateinit var categorySpinner: Spinner
    private lateinit var customCategoryEditText: EditText

    /**
     * Initializes the activity, setting up UI elements and loading the current coupon data.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_coupon)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = intent.extras!!.getSerializable("userObject") as User
        currentCoupon = intent.extras!!.getSerializable("couponObject") as Coupon
        index = intent.extras!!.getInt("couponIndex")

        initDatePicker()

        categorySpinner = findViewById(R.id.category_spinner)
        customCategoryEditText = findViewById(R.id.custom_category_edittext)

        // Setup Spinner with categories
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Other (Custom)") {
                    customCategoryEditText.visibility = View.VISIBLE
                } else {
                    customCategoryEditText.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                customCategoryEditText.visibility = View.GONE
            }
        }
        loadCoupon()
    }

    /**
     * Loads the current coupon's details into the UI components.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCoupon() {
        findViewById<TextInputEditText>(R.id.couponTitle).setText(currentCoupon.Title)
        findViewById<TextInputEditText>(R.id.couponCode).setText(currentCoupon.Code)
        findViewById<TextInputEditText>(R.id.couponCompany).setText(currentCoupon.Company)
        customCategoryEditText.setText(currentCoupon.Category)
        if (currentCoupon.Expire_Date != null) {
            findViewById<Button>(R.id.expirationDatePicker).text =
                currentCoupon.Expire_Date!!.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
        findViewById<CheckBox>(R.id.is_used).isChecked = currentCoupon.Is_Used
        findViewById<TextInputEditText>(R.id.boughtFrom).setText(currentCoupon.Bought_From)
        findViewById<TextInputEditText>(R.id.couponDescription).setText(currentCoupon.Description)
    }

    /**
     * Handles the category selection from the Spinner, including custom category input.
     *
     * @return The final category to be used for the coupon.
     */
    private fun handleCategorySubmission(): String {
        val selectedCategory = categorySpinner.selectedItem.toString()
        val customCategory = customCategoryEditText.text.toString()

        val finalCategory =
            if (selectedCategory == "Other (Custom)" && customCategory.isNotBlank()) {
                customCategory
            } else {
                selectedCategory
            }
        return finalCategory
    }

    /**
     * Initializes the date picker for selecting the coupon's expiration date.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDatePicker() {
        val datePickerButton: Button = findViewById<Button>(R.id.expirationDatePicker)
        datePickerButton.setOnClickListener {
            // init calendar to get current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, {_, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                datePickerButton.text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }, year, month, day)

            datePickerDialog.show()
        }
    }

    /**
     * Submits the edited coupon data and updates it on the server.
     *
     * @param view The view that triggered this method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun editCoupon(view: View) {
        val title = findViewById<TextInputEditText>(R.id.couponTitle).text.toString()
        val code = findViewById<TextInputEditText>(R.id.couponCode).text.toString()
        val company = findViewById<TextInputEditText>(R.id.couponCompany).text.toString()
        val category = handleCategorySubmission()
        val is_used = findViewById<CheckBox>(R.id.is_used).isChecked

        if (code.isEmpty()) {
            val noCode = "Please add a coupon code."
            Toast.makeText(this@EditCouponActivity, noCode, Toast.LENGTH_SHORT).show()
            return
        }
        val dateStr = findViewById<Button>(R.id.expirationDatePicker).text.toString()
        if (dateStr.isEmpty()) {
            val noDate = "Please add a coupon date."
            Toast.makeText(this@EditCouponActivity, noDate, Toast.LENGTH_SHORT).show()
            return
        }
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val desc = findViewById<TextInputEditText>(R.id.couponDescription).text.toString()
        val bought_from = findViewById<TextInputEditText>(R.id.boughtFrom).text.toString()

        val editedCoupon = Coupon(
            Title = title,
            Company = company,
            Category = category,
            Expire_Date = date,
            Is_Used = is_used,
            Description = desc,
            Code = code,
            Bought_From = bought_from
        )

        val newCouponApi = CouponAPI()
        newCouponApi.UpdateFieldsFromCoupon(editedCoupon)
        serverAPIManager.updateCouponUserIndex(currentUser.userName, index, newCouponApi, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) { // successful response
                    val couponUpdated = "Coupon was successfully updated!"
                    Toast.makeText(this@EditCouponActivity, couponUpdated, Toast.LENGTH_SHORT).show()
                    updateUserEmbeddings(newCouponApi)
                }
                else {
                    val couponNotUpdated = "The coupon was not updated. ${response.code()}"
                    Toast.makeText(this@EditCouponActivity, couponNotUpdated, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i("COUPON_EDIT_FAILURE", "onFailure: ${t.message}")
            }
        })
    }

    /**
     * Updates the user's embeddings based on the coupon changes.
     *
     * @param newCouponApi The updated coupon data.
     */
    private fun updateUserEmbeddings(newCouponApi: CouponAPI) {
        val couponDesc = newCouponApi.category + newCouponApi.title + newCouponApi.company + newCouponApi.description
        val oldCouponDesc = currentCoupon.Category + currentCoupon.Title + currentCoupon.Company + currentCoupon.Description
        serverAPIManager.updateUserAverageEmbeddingOnEdit(currentUser.userName!!, couponDesc,
            oldCouponDesc, object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) { // successful response
                        val couponAdded = "User embedding successfully updated!"
                        Toast.makeText(this@EditCouponActivity, couponAdded, Toast.LENGTH_SHORT).show()
                        alertBrowseCouponsRefresh()
                    }
                    else {
                        val couponNotAdded = "User embedding was not updated. ${response.code()}"
                        Toast.makeText(this@EditCouponActivity, couponNotAdded, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("COUPON_EDIT_EMBED_FAILURE", "onFailure: ${t.message}")
                }
            }
        )
    }

    /**
     * Triggers a refresh of the coupon list in the browsing activity.
     */
    private fun alertBrowseCouponsRefresh() {
        val resultIntent = Intent()
        resultIntent.putExtra("coupon_updated", true)
        setResult(android.app.Activity.RESULT_OK, resultIntent)
    }
}