package com.lyadirga.bildirimleogren.ui.setdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.lyadirga.bildirimleogren.databinding.FragmentSetDetailBinding
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetDetailFragment: BaseFragment<FragmentSetDetailBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetDetailBinding {
        return FragmentSetDetailBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
    }

    override fun observeFlows() {
    }
}