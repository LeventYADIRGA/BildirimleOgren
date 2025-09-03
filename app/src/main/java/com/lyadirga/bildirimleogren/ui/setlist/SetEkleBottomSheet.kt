package com.lyadirga.bildirimleogren.ui.setlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lyadirga.bildirimleogren.databinding.BottomSheetSetEkleBinding
import com.lyadirga.bildirimleogren.ui.base.BaseBottomSheetFragment

class SetEkleBottomSheet(private val onUrlAdded: (String) -> Unit): BaseBottomSheetFragment<BottomSheetSetEkleBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetSetEkleBinding {
        return BottomSheetSetEkleBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ekleButton.setOnClickListener {
            binding.etUrlLayout.error = null
            val url = binding.etUrl.text?.trim().toString()
            if (url.isEmpty()) {
                binding.etUrlLayout.error = "Lütfen E Tablolar url i giriniz."
            } else {
                dismiss()
                binding.etUrl.text = null
                onUrlAdded(url)
            }
        }
    }
}