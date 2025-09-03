package com.lyadirga.bildirimleogren.ui.setdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.lyadirga.bildirimleogren.databinding.FragmentSetDetailBinding
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import com.lyadirga.bildirimleogren.ui.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SetDetailFragment: BaseFragment<FragmentSetDetailBinding>() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: SetDetailFragmentArgs by navArgs() // Safe Args


    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetDetailBinding {
        return FragmentSetDetailBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
    }

    override fun observeFlows() {
        viewModel.getSetDetails(args.setId) { setDetails ->
            if (setDetails != null) {
                // TODO: Veriyi UI'ye bağla


            } else {
                requireContext().showToast("Set bulunamadı")
            }
        }
    }
}