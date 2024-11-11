package com.example.couponverse

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.couponverse.data.Product
import com.example.couponverse.data.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.content.ContextCompat

/**
 * Activity for displaying and managing a list of products from the ML recommendations.
 *
 * Provides functionality to:
 * - Fetch products from the server based on user data.
 * - Display products in a grid layout.
 * - Sort products by stars or price.
 * - Filter products based on star ratings and price ranges.
 */
class MLProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val serverAPIManager = RetrofitAPIManager.getService(this)
    private var productsList: MutableList<Product> = mutableListOf<Product>()
    private val originalProductList = mutableListOf<Product>()
    private val GRID_ROW_NUM = 2

    /**
     * Initializes the activity, setting up UI elements and fetching product data.
     *
     * @param savedInstanceState The saved instance state for restoring the activity's state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_products)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, GRID_ROW_NUM)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)

        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        productAdapter = ProductAdapter(productsList)
        recyclerView.adapter = productAdapter

        fetchProducts()

        // Set up sorting spinner
        setUpSortButton()

        // Set up filter options
        setUpFilterButton()

        // Set up reset button
        setUpResetButton()
    }

    /**
     * Fetches product data from the server based on the user's average embedding.
     */
    private fun fetchProducts() {
        val user = intent.extras!!.getSerializable("userObject") as User
        serverAPIManager.getMLProducts(user.averageEmbedding!!, object : Callback<List<Product>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    originalProductList.clear()
                    originalProductList.addAll(response.body()!!)
                    productsList.clear()
                    productsList.addAll(response.body()!!)
                    productAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@MLProductsActivity, "Failed to load products.", Toast.LENGTH_SHORT).show()
                    productsList.clear()
                    productAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@MLProductsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                productsList.clear()
            }
        })
    }


    /**
     * Sets up the button for resestting the custom order and filteres.
     */
    private fun setUpResetButton() {
        val btnSort: Button = findViewById(R.id.btnReset)

        btnSort.setOnClickListener {
            productsList.clear()
            productsList.addAll(originalProductList)
            productAdapter.notifyDataSetChanged()
        }
    }


    /**
     * Sets up the button for sorting products.
     */
    private fun setUpSortButton() {
        val btnSort: Button = findViewById(R.id.btnSort)

        btnSort.setOnClickListener {
            val popupMenu = PopupMenu(this, btnSort)
            popupMenu.menuInflater.inflate(  R.menu.sort_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.sort_by_stars_asc -> {
                        sortByStars(ascending = true)
                        true
                    }
                    R.id.sort_by_stars_desc -> {
                        sortByStars(ascending = false)
                        true
                    }
                    R.id.sort_by_price_asc -> {
                        sortByPrice(ascending = true)
                        true
                    }
                    R.id.sort_by_price_desc -> {
                        sortByPrice(ascending = false)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    /**
     * Sorts the product list by stars.
     *
     * @param ascending Whether to sort in ascending order.
     */
    private fun sortByStars(ascending: Boolean) {
        if (ascending) {
            productsList.sortBy { it.stars }
        } else {
            productsList.sortByDescending { it.stars }
        }
        productAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }

    /**
     * Sorts the product list by price.
     *
     * @param ascending Whether to sort in ascending order.
     */
    private fun sortByPrice(ascending: Boolean) {
        if (ascending) {
            productsList.sortBy { it.price }
        } else {
            productsList.sortByDescending { it.price }
        }
        productAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }

    /**
     * Sets up the button for filtering products.
     */
    private fun setUpFilterButton() {
        val btnFilter: ImageButton = findViewById(R.id.btnFilter)

        btnFilter.setOnClickListener {
            val popupView = LayoutInflater.from(this).inflate(R.layout.filter_popup, null)
            val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
            // Show the popup window
            popupWindow.showAsDropDown(btnFilter)

            val editTextMinStars: EditText = popupView.findViewById(R.id.editTextMinStars)
            val editTextMaxStars: EditText = popupView.findViewById(R.id.editTextMaxStars)
            val editTextMinPrice: EditText = popupView.findViewById(R.id.editTextMinPrice)
            val editTextMaxPrice: EditText = popupView.findViewById(R.id.editTextMaxPrice)
            val btnFilterApply: Button = popupView.findViewById(R.id.btnFilterApply)

            btnFilterApply.setOnClickListener {
                val minStarsText = editTextMinStars.text.toString().takeIf { it.isNotEmpty() }
                val maxStarsText = editTextMaxStars.text.toString().takeIf { it.isNotEmpty() }
                val minPriceText = editTextMinPrice.text.toString().takeIf { it.isNotEmpty() }
                val maxPriceText = editTextMaxPrice.text.toString().takeIf { it.isNotEmpty() }

                val minStars = minStarsText?.toDoubleOrNull()
                val maxStars = maxStarsText?.toDoubleOrNull()
                val minPrice = minPriceText?.toDoubleOrNull()
                val maxPrice = maxPriceText?.toDoubleOrNull()

                applyFilters(minStars, maxStars, minPrice, maxPrice)
                popupWindow.dismiss()
            }
        }
    }


    /**
     * Applies filters to the product list based on star ratings and price ranges.
     *
     * @param minStars Minimum star rating.
     * @param maxStars Maximum star rating.
     * @param minPrice Minimum price.
     * @param maxPrice Maximum price.
     */
    private fun applyFilters(minStars: Double?, maxStars: Double?, minPrice: Double?, maxPrice: Double?) {
        val filteredList = productsList.filter { product ->
            val starsCondition = (minStars == null || product.stars >= minStars) && (maxStars == null || product.stars <= maxStars)
            val priceCondition = (minPrice == null || product.price >= minPrice) && (maxPrice == null || product.price <= maxPrice)

            starsCondition && priceCondition
        }
        productsList.clear()
        productsList.addAll(filteredList)
        productAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(0)
    }
}
