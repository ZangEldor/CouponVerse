package com.example.couponverse

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.couponverse.data.Coupon
import java.time.LocalDate
import java.time.Period

/**
 * Adapter for displaying a list of coupons in a RecyclerView.
 *
 * Binds data from the list of coupons to the views in each item of the RecyclerView.
 *
 * @property couponList The list of coupons to be displayed.
 */
class CouponAdapter(private val couponList: List<Coupon>) : RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    /**
     * ViewHolder for holding references to views in each coupon item layout.
     *
     * @property couponTitle The TextView displaying the coupon title.
     * @property couponCode The TextView displaying the coupon code.
     * @property couponExpiryStatus The TextView displaying the status of the coupon's expiration.
     * @property couponExpiryDate The TextView displaying the coupon's expiry date.
     */
    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val couponTitle: TextView = itemView.findViewById(R.id.couponTitle)
        val couponCode: TextView = itemView.findViewById(R.id.couponCode)
        val couponExpiryStatus: TextView = itemView.findViewById(R.id.couponExpiryStatus)
        val couponExpiryDate: TextView = itemView.findViewById(R.id.couponExpiryDate)
    }

    /**
     * Inflates the layout for each coupon item and returns a CouponViewHolder.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The type of the view.
     * @return A new instance of CouponViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.coupon_item, parent, false)
        return CouponViewHolder(itemView)
    }

    /**
     * Calculates and returns the status of the coupon based on its expiration date and usage status.
     *
     * @param expiration The expiration date of the coupon.
     * @param isUsed Boolean indicating whether the coupon has been used.
     * @return A string representing the status of the coupon.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun couponStatus(expiration: LocalDate, isUsed: Boolean) : String {
        if (isUsed)
            return "Used."
        val period : Period = Period.between(LocalDate.now(), expiration)
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
     * Binds the coupon data to the views for the current item.
     *
     * @param holder The ViewHolder that holds the views for the current item.
     * @param position The position of the item within the adapter's data set.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        val coupon = couponList[position]

        holder.couponTitle.text = coupon.Title
        holder.couponCode.text = coupon.Code
        holder.couponExpiryStatus.text = couponStatus(coupon.Expire_Date!!, coupon.Is_Used)
        holder.couponExpiryDate.text = coupon.Expire_Date.toString()
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The size of the coupon list.
     */
    override fun getItemCount() = couponList.size
}