package com.example.couponverse

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.couponverse.data.User
import com.example.couponverse.data.Coupon
import com.example.couponverse.data.CouponAI
import com.example.couponverse.data.CouponAPI
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Activity for adding a new coupon, including UI for entering coupon details and interacting with the server.
 *
 * Provides functionality to:
 * - Show a date picker for selecting the coupon expiration date.
 * - Display and handle a dialog for entering coupon text.
 * - Submit a new coupon to the server.
 * - Update the user's average embedding based on the new coupon.
 * - Reset the form fields to their default states.
 */
class AddCouponActivity : AppCompatActivity() {

    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private lateinit var user: User
    private lateinit var categorySpinner: Spinner
    private lateinit var customCategoryEditText: EditText
    private lateinit var operation: String
    private lateinit var categories_array: Array<String>

    /**
     * Initializes the activity, setting up the user object, UI elements, and listeners.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_coupon)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        user = intent.extras!!.getSerializable("userObject") as User
        operation = intent.extras!!.getSerializable("operation") as String

        initDatePicker() // https://www.youtube.com/watch?v=qCoidM98zNk

        val btnShowDialog = findViewById<Button>(R.id.extractTextButton)
        btnShowDialog.setOnClickListener {
            showCouponDialog()
        }

        categories_array = resources.getStringArray(R.array.category_array)
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
        // Watch for text changes in the "Other" text box and if the text matches one of the categories than pick it instead
        customCategoryEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val enteredText = editable.toString()

                // Check if the entered text matches any category
                categories_array.forEachIndexed { index, category ->
                    if (enteredText.equals(category, ignoreCase = true)) {
                        // Reset the text box and update the spinner
                        customCategoryEditText.setText("")
                        categorySpinner.setSelection(index)
                        customCategoryEditText.visibility = EditText.GONE
                        return@forEachIndexed
                    }
                }
            }
        })

        // update fields if edit coupon activity is enabled
        if (operation == "update"){
            var coupon = intent.extras!!.getSerializable("couponObject") as Coupon
            findViewById<TextView>(R.id.textViewTitle).text = "Edit Coupon"
            findViewById<Button>(R.id.addCouponButton).setText("Update Coupon")
            fillCouponFields(coupon)
        }
    }

    /**
     * Handles the submission of the selected category from the spinner, allowing for custom categories.
     *
     * @return The  category string, either the selected item or a custom value.
     */
    private fun handleCategorySubmission(): String {
        val selectedCategory = categorySpinner.selectedItem.toString()
        val customCategory = customCategoryEditText.text.toString()

        return if (selectedCategory == "Other (Custom)" && customCategory.isNotBlank()) {
            customCategory
        } else {
            selectedCategory
        }
    }

    /**
     * Initializes the date picker dialog for selecting the coupon expiration date.
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
     * Submits a new coupon to the server and handles the response.
     *
     * @param view The view that triggered this method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addNewCoupon(view: View) {
        val title = findViewById<TextInputEditText>(R.id.couponTitle).text.toString()
        val code = findViewById<TextInputEditText>(R.id.couponCode).text.toString()
        val company = findViewById<TextInputEditText>(R.id.couponCompany).text.toString()
        val category = handleCategorySubmission()
        val is_used = findViewById<CheckBox>(R.id.is_used).isChecked

        if (code.isEmpty()) {
            val noCode = "Please add a coupon code."
            Toast.makeText(this@AddCouponActivity, noCode, Toast.LENGTH_SHORT).show()
            return
        }
        val dateStr = findViewById<Button>(R.id.expirationDatePicker).text.toString()
        if (dateStr.isEmpty()) {
            val noDate = "Please add a coupon date."
            Toast.makeText(this@AddCouponActivity, noDate, Toast.LENGTH_SHORT).show()
            return
        }
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val desc = findViewById<TextInputEditText>(R.id.couponDescription).text.toString()
        val bought_from = findViewById<TextInputEditText>(R.id.boughtFrom).text.toString()

        val newCoupon = Coupon(
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
        newCouponApi.UpdateFieldsFromCoupon(newCoupon)

        // case for adding a new couopon
        if(operation == "add") {
            serverAPIManager.addNewCouponUser(user, newCouponApi, object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) { // successful response
                        val couponAdded = "New coupon was successfully added!"
                        Toast.makeText(this@AddCouponActivity, couponAdded, Toast.LENGTH_SHORT)
                            .show()
                        updateUserEmbeddings(newCouponApi)
                    } else {
                        val couponNotAdded = "The coupon was not added. ${response.code()}"
                        Toast.makeText(this@AddCouponActivity, couponNotAdded, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("COUPON_MANUAL_ADD_FAILURE", "onFailure: ${t.message}")
                }
            })
        }
        // else case for updating
        else {
            var index = intent.extras!!.getSerializable("couponIndex") as Int
            serverAPIManager.updateCouponUserIndex(user.userName, index, newCouponApi, object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) { // successful response
                        val couponUpdated = "Coupon was successfully updated!"
                        Toast.makeText(this@AddCouponActivity, couponUpdated, Toast.LENGTH_SHORT).show()
                        updateUserEmbeddings(newCouponApi)
                    }
                    else {
                        val couponNotUpdated = "The coupon was not updated. ${response.code()}"
                        Toast.makeText(this@AddCouponActivity, couponNotUpdated, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("COUPON_EDIT_FAILURE", "onFailure: ${t.message}")
                }
            })
        }
    }

    /**
     * Updates the user's average embedding based on the new coupon and handles the response.
     *
     * @param newCouponApi The `CouponAPI` object containing the new coupon data.
     */
    private fun updateUserEmbeddings(newCouponApi: CouponAPI) {
        val couponDesc = newCouponApi.category + newCouponApi.title + newCouponApi.company + newCouponApi.description
        serverAPIManager.updateUserAverageEmbedding(user.userName!!, couponDesc, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) { // successful response
                    val couponAdded = "User embedding successfully updated!"
                    Toast.makeText(this@AddCouponActivity, couponAdded, Toast.LENGTH_SHORT).show()
                    // return to previous menu
                    finish()
                }
                else {
                    val couponNotAdded = "User embedding was not updated. ${response.code()}"
                    Toast.makeText(this@AddCouponActivity, couponNotAdded, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i("COUPON_ADD_EMBED_FAILURE", "onFailure: ${t.message}")
            }
        })
    }

    /**
     * Shows a dialog for entering coupon text and processes it with AI.
     */
    fun showCouponDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Coupon Text")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            isSingleLine = false
            maxLines = 5
        }
        builder.setView(input)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val couponText = input.text.toString()
            // Call the Retrofit function with the coupon text
            submitCoupon(couponText)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    /**
     * Submits the coupon text to the server for AI analysis and updates the UI with the results.
     *
     * @param couponText The text of the coupon to be analyzed by AI.
     */
    fun submitCoupon(couponText: String) {
        serverAPIManager.extractNewCoupon(couponText, object : Callback<CouponAI> {
            override fun onResponse(call: Call<CouponAI>, response: Response<CouponAI>) {
                if (response.isSuccessful) { // successful response
                    try {
                        val couponAIFields = response.body()

                        val codeField = findViewById<EditText>(R.id.couponCode)
                        codeField.setText(couponAIFields?.coupon_code ?: "")
                        val titleField = findViewById<EditText>(R.id.couponTitle)
                        titleField.setText(couponAIFields?.title ?: "")
                        val categoryField = findViewById<EditText>(R.id.custom_category_edittext)
                        categoryField.setText(couponAIFields?.category ?: "")
                        val companyField = findViewById<EditText>(R.id.couponCompany)
                        companyField.setText(couponAIFields?.company ?: "")
                        val descriptionField = findViewById<EditText>(R.id.couponDescription)
                        descriptionField.setText(couponAIFields?.description ?: "")

                        val expirationDateString = couponAIFields?.expiration_date
                        if (expirationDateString != null) {
                            val expirationDate = LocalDate.parse(expirationDateString)
                            val year = expirationDate.year
                            val month = expirationDate.monthValue - 1 // Month in DatePicker is 0-indexed
                            val day = expirationDate.dayOfMonth
                            val datePicker = findViewById<Button>(R.id.expirationDatePicker)

                            // Update the DatePicker with the parsed date
                            datePicker.text = expirationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }
                    } catch (e: Exception) {
                        val parsingError = "AI parsing error"
                        Toast.makeText(this@AddCouponActivity, parsingError, Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    val parsingError = "AI parsing error"
                    Toast.makeText(this@AddCouponActivity, parsingError, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CouponAI>, t: Throwable) {
                Toast.makeText(this@AddCouponActivity, "Failed to use AI analyzer, please insert manually.", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@AddCouponActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }


    /**
     * Submits the coupon text to the server for AI analysis and updates the UI with the results.
     *
     * @param coupon the coupon object to update from
     */
    @SuppressLint("NewApi")
    fun fillCouponFields(coupon: Coupon) {
        findViewById<TextInputEditText>(R.id.couponTitle).setText(coupon.Title)
        findViewById<EditText>(R.id.custom_category_edittext).setText(coupon.Category)
        findViewById<TextInputEditText>(R.id.couponCompany).setText(coupon.Company)
        findViewById<TextInputEditText>(R.id.couponCode).setText(coupon.Code)
        findViewById<Button>(R.id.expirationDatePicker).text = coupon.Expire_Date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        findViewById<CheckBox>(R.id.is_used).isChecked = coupon.Is_Used
        findViewById<TextInputEditText>(R.id.boughtFrom).setText(coupon.Bought_From)
        findViewById<TextInputEditText>(R.id.couponDescription).setText(coupon.Description)
    }


    /**
     * Resets all the form fields to their default states.
     *
     * @param view The view that triggered this method.
     */
    fun resetFields(view: View) {
        findViewById<TextInputEditText>(R.id.couponTitle).setText("")
        findViewById<EditText>(R.id.custom_category_edittext).setText("")
        findViewById<TextInputEditText>(R.id.couponCompany).setText("")
        findViewById<TextInputEditText>(R.id.couponCode).setText("")
        findViewById<Button>(R.id.expirationDatePicker).text = ""
        findViewById<CheckBox>(R.id.is_used).isChecked = false
        findViewById<TextInputEditText>(R.id.boughtFrom).setText("")
        findViewById<TextInputEditText>(R.id.couponDescription).setText("")
    }
}