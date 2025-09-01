package com.lyadirga.bildirimleogren.ui.base

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding


abstract class BaseActivity<VB: ViewBinding> : AppCompatActivity(){

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun createBinding(inflater: LayoutInflater): VB
    protected abstract fun prepareView(savedInstanceState: Bundle?)
    protected abstract fun observeViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = createBinding(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdgeAndStatusBar(binding.root)
        observeViewModel()
        prepareView(savedInstanceState)
    }

    private fun setupEdgeToEdgeAndStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    v.paddingLeft,
                    systemBarsInsets.top,
                    v.paddingRight,
                    systemBarsInsets.bottom
                )

                val isDarkTheme = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> true   // Koyu mod
                    Configuration.UI_MODE_NIGHT_NO -> false   // Açık mod
                    else -> false
                }

                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

                // true -> ikonlar koyu, false -> ikonlar açık
                windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme

                insets
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}