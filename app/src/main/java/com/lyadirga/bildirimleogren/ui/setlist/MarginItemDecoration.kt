package com.lyadirga.bildirimleogren.ui.setlist

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val verticalSpaceHeight: Int, private val horizontalSpaceWidth: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        // Set top margin
        outRect.top = verticalSpaceHeight

        // Set left and right margins
        outRect.left = horizontalSpaceWidth
        outRect.right = horizontalSpaceWidth

        // 🇹🇷Türkçe: Eğer son item değilse, alt margini de ekle
        // 🇬🇧English: If not the last item, also add bottom margin
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}