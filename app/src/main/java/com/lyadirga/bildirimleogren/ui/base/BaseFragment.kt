package com.lyadirga.bildirimleogren.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding?> : Fragment()  {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected abstract fun prepareView(savedInstanceState: Bundle?)
    protected abstract fun observeFlows()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFlows()
        prepareView(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}