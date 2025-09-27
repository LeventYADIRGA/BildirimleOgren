package com.lyadirga.borsadefterim.ui.bilgi

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.databinding.FragmentBilgiBinding
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import androidx.core.net.toUri


class BilgiFragment : BaseFragment<FragmentBilgiBinding>() {

    companion object {
        private const val GOOGLE_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSeBcfoT_eMMj6z1sG7P4t2_AAzd49bUiII2nQ8GzdZ2BQTr6A/viewform?usp=header"
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBilgiBinding {
        return FragmentBilgiBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbarBilgi.setupWithNavController(navController, appBarConfiguration)

        val bilgiBody = getString(R.string.info_body)
        binding.body.text = Html.fromHtml(bilgiBody, Html.FROM_HTML_MODE_LEGACY)

        binding.btnFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, GOOGLE_FORM_URL.toUri())
            startActivity(intent)
        }

    }

    override fun observeFlows() {}
}