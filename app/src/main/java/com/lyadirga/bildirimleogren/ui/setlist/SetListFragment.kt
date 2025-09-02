package com.lyadirga.bildirimleogren.ui.setlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.lyadirga.bildirimleogren.databinding.FragmentSetListBinding
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetListFragment: BaseFragment<FragmentSetListBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetListBinding {
        return FragmentSetListBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {}

    override fun observeFlows() {}
}