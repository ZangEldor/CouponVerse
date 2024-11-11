package com.example.couponverse

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.couponverse.data.Product


/**
 * Adapter for displaying a list of products in a RecyclerView.
 *
 * @property products The list of products to be displayed.
 */
class ProductAdapter(private val products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    /**
     * Creates a new ViewHolder for the product item view.
     *
     * @param parent The parent ViewGroup that this ViewHolder's view will be attached to.
     * @param viewType The view type of the new View.
     * @return A new ProductViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    /**
     * Binds the product data to the specified ViewHolder.
     *
     * @param holder The ViewHolder to bind the data to.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    /**
     * Returns the number of items in the data set.
     *
     * @return The number of items in the data set.
     */
    override fun getItemCount(): Int = products.size

    /**
     * ViewHolder for product items.
     *
     * @property itemView The view for each item in the RecyclerView.
     */
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        private val txtBestSeller: TextView = itemView.findViewById(R.id.txtBestSeller)
        private val txtStars: TextView = itemView.findViewById(R.id.txtStars)
        private val imgStar: ImageView = itemView.findViewById(R.id.imgStar)
        private val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)

        /**
         * Binds the product data to the view components.
         *
         * @param product The Product object containing the data to be displayed.
         */
        fun bind(product: Product) {
            Glide.with(itemView.context)
                .load(product.imgUrl)
                .fitCenter()
                .into(imgProduct)

            txtTitle.text = product.title
            txtPrice.text = "$${product.price}" // Display price with currency symbol
            txtStars.text = product.stars.toString()
            txtCategory.text = product.category_name

            // Show or hide "Best Seller" label
            if (product.isBestSeller) {
                txtBestSeller.visibility = View.VISIBLE
            } else {
                txtBestSeller.visibility = View.GONE
            }

            // Handle item click to open product URL
            itemView.setOnClickListener {
                openProductUrl(itemView.context, product.productURL)
            }
            // Adjust star image size to match text size
            val textSize = txtStars.textSize // Get the text size in pixels
            imgStar.layoutParams.width = textSize.toInt() // Set width to match text size
            imgStar.layoutParams.height = textSize.toInt() // Set height to match text size
            imgStar.requestLayout() // Request layout update
        }

        /**
         * Opens the product URL in a browser.
         *
         * @param context The context from which to start the activity.
         * @param url The URL to open.
         */
        private fun openProductUrl(context: Context, url: String) {
            if( url != null)
            {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }
}




