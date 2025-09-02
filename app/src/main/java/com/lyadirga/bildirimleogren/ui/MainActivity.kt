package com.lyadirga.bildirimleogren.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.databinding.ActivityMainBinding
import com.lyadirga.bildirimleogren.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment


    override fun createBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun observeFlows() {}
}