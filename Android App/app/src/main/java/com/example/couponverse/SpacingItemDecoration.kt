package com.example.couponverse

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom ItemDecoration for RecyclerView that adds spacing around each item.
 *
 * This decoration adds equal spacing to the left, right, top, and bottom of each item
 * in the RecyclerView. It is useful for creating uniform padding between items.
 *
 * @property spacing The amount of spacing (in pixels) to apply around each item.
 */
class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    /**
     * Sets the offsets for each item in the RecyclerView, applying the specified spacing.
     *
     * @param outRect The output rectangle to receive the item offsets.
     * @param view The view of the item that is being offset.
     * @param parent The RecyclerView to which this item belongs.
     * @param state The current state of the RecyclerView.
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.top = spacing
        outRect.bottom = spacing
    }
}
