package com.naposystems.pepito.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ActivityMainBinding
import com.naposystems.pepito.utility.LocaleHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.landingFragment,
                R.id.languageSelectionDialog -> hideToolbar()
                else -> showToolbar()
            }
        }
    }

    private fun hideToolbar() {
        with(binding.toolbar) {
            visibility = View.GONE
        }
    }

    private fun showToolbar() {
        with(binding.toolbar) {
            visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp(appBarConfiguration)

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }
}
