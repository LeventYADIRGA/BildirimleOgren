package com.lyadirga.bildirimleogren.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetFragment<VB : ViewBinding?> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    /**
     * 🇹🇷Türkçe:
     * BottomSheet'i göstermek için tek parametreli show.
     * TAG otomatik olarak sınıf adı kullanılır.
     *
     * 🇬🇧English:
     * Show BottomSheet with a single parameter.
     * TAG is automatically set to the class name.
     */
    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, this::class.java.simpleName)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}