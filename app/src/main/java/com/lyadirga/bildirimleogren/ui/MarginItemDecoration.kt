package com.lyadirga.bildirimleogren.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val verticalSpaceHeight: Int, private val horizontalSpaceWidth: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        // Üst margin belirle
        outRect.top = verticalSpaceHeight

        // Sol ve sağ margin belirle
        outRect.left = horizontalSpaceWidth
        outRect.right = horizontalSpaceWidth

        // Eğer son item değilse, alt margini de ekle
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}
