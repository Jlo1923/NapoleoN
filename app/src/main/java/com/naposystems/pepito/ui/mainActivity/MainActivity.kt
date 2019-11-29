package com.naposystems.pepito.ui.mainActivity

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ActivityMainBinding
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: MainActivityViewModel

    private val options by lazy {
        navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MainActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment
            ), binding.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.landingFragment,
                R.id.languageSelectionDialog,
                R.id.registerFragment -> {
                    hideToolbar()
                    disableDrawer()
                }
                R.id.homeFragment -> {
                    showToolbar()
                    enableDrawer()
                }
                else -> {
                    showToolbar()
                    disableDrawer()
                }
            }
        }

        viewModel.user.observe(this, Observer {
            if (it != null) {
                updateHeaderDrawer(it)
            }
        })

        viewModel.errorGettingUser.observe(this, Observer {
            if (it == true) {
                val message = getString(R.string.something_went_wrong)

                Utils.showSimpleSnackbar(binding.coordinator, message, 2)
            }
        })

        binding.navView.setNavigationItemSelectedListener(this)

        setMarginToNavigationView()

    }

    private fun setMarginToNavigationView() {
        val height: Int
        val myResources: Resources = resources
        val idStatusBarHeight: Int =
            myResources.getIdentifier("status_bar_height", "dimen", "android")
        if (idStatusBarHeight > 0) {
            height = resources.getDimensionPixelSize(idStatusBarHeight)

            val layoutParams: DrawerLayout.LayoutParams =
                binding.navView.layoutParams as DrawerLayout.LayoutParams
            layoutParams.setMargins(0, height, 0, 0)
        }
    }

    fun getUser() {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        viewModel.getUser(firebaseId)
    }

    private fun updateHeaderDrawer(user: User) {
        val headerView = binding.navView.getHeaderView(0)

        val imageViewBackground = headerView.findViewById<ImageView>(R.id.imageView_background)
        val imageViewAvatar = headerView.findViewById<ImageView>(R.id.imageView_profile_image)
        val textViewDisplayName = headerView.findViewById<TextView>(R.id.textView_user_name)
        val textViewNickname = headerView.findViewById<TextView>(R.id.textView_user_nickname)

        val defaultAvatar = resources.getDrawable(
            R.drawable.ic_default_avatar,
            this.theme
        )

        val defaultHeaderBackground = resources.getDrawable(
            R.drawable.bg_default_drawer_header,
            this.theme
        )

        val imageUrl = user.imageUrl

        Glide.with(this)
            .load(
                if (imageUrl.isEmpty()) defaultAvatar else imageUrl
            )
            .circleCrop()
            .into(imageViewAvatar)

        Glide.with(this)
            .load(if (user.headerUri.isEmpty()) defaultHeaderBackground else Uri.parse(user.headerUri))
            .into(imageViewBackground)

        textViewDisplayName.text = user.displayName

        val nickname = getString(R.string.label_nickname, user.nickname)

        textViewNickname.text = nickname

        headerView.setOnClickListener {
            navController.navigate(R.id.profileFragment, null, options)
        }
    }

    private fun hideToolbar() {
        binding.toolbar.apply {
            visibility = View.GONE
        }
    }

    private fun disableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun showToolbar() {
        binding.toolbar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(navController, binding.drawerLayout)

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        binding.drawerLayout.closeDrawers()

        when (menuItem.itemId) {
            R.id.security_settings -> navController.navigate(R.id.securitySettingsFragment)
            R.id.appearance_settings -> navController.navigate(R.id.appearanceSettingsFragment)
            R.id.invite_someone -> navController.navigate(R.id.inviteSomeoneFragment)
            R.id.help -> navController.navigate(R.id.helpFragment)
        }

        return true
    }
}
