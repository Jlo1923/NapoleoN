package com.naposystems.pepito.ui.mainActivity

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavOptions
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
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: MainActivityViewModel
//    private var timeLockApp: Long = 0L
    private var timeRequestAccessPin: Int = 0
    private var accountStatus: Int = 0

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

        viewModel.getTheme()
        viewModel.getAccountStatus()
        viewModel.accountStatus.observe(this, Observer {
            accountStatus = it
        })

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

        //Traer Preferencia

        navController.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.getAccountStatus()
            when (destination.id) {
                R.id.splashFragment,
                R.id.landingFragment,
                R.id.registerFragment,
                R.id.previewImageSendFragment,
                R.id.enterPinFragment,
                R.id.unlockAppTimeFragment -> {
                    hideToolbar()
                    disableDrawer()
                }
                R.id.homeFragment -> {
                    showToolbar()
                    enableDrawer()
                }
                R.id.conversationFragment -> {
                    showToolbar()
                    binding.toolbar.title = ""
                    binding.toolbar.setContentInsetsAbsolute(0, 0)
                    binding.toolbar.elevation = 0f
                    binding.toolbar.setBackgroundColor(
                        resources.getColor(
                            R.color.flatActionBarColor,
                            this.theme
                        )
                    )
                }
                R.id.accessPinFragment -> {
                    if (accountStatus == Constants.AccountStatus.ACCOUNT_RECOVERED.id) {
                        hideToolbar()
                        disableDrawer()
                    } else {
                        showToolbar()
                    }
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
                val message = getString(R.string.text_fail)

                Utils.showSimpleSnackbar(binding.coordinator, message, 2)
            }
        })

        viewModel.theme.observe(this, Observer {
            val theme = when (it) {
                Constants.ColorScheme.LIGHT_THEME.scheme -> AppCompatDelegate.MODE_NIGHT_NO
                Constants.ColorScheme.DARK_THEME.scheme -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }

            AppCompatDelegate.setDefaultNightMode(theme)
        })

        viewModel.timeAccessPin.observe(this, Observer {
            timeRequestAccessPin = it
        })

//        viewModel.blockTime.observe(this, Observer {
//            timeLockApp = it
//        })

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
        viewModel.getUser()
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
            elevation = Utils.dpToPx(context!!, 4f).toFloat()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        LocaleHelper.setLocale(this)
        super.onConfigurationChanged(newConfig)
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
            R.id.security_settings -> navController.navigate(
                R.id.securitySettingsFragment,
                null,
                options
            )
            R.id.appearance_settings -> navController.navigate(
                R.id.appearanceSettingsFragment,
                null,
                options
            )
            R.id.invite_someone -> navController.navigate(R.id.inviteSomeoneFragment, null, options)
            R.id.help -> navController.navigate(R.id.helpFragment, null, options)
        }

        return true
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onResume() {
        super.onResume()
        validLockTime()
        showContent()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setLockTimeApp()
        viewModel.getTimeRequestAccessPin()
        hideContent()
    }

    override fun onStop() {
        super.onStop()
        hideContent()
    }

    private fun hideContent() {
        binding.screenSaver.visibility = View.VISIBLE
        binding.imageViewLogoApp.visibility = View.VISIBLE
    }

    private fun showContent() {
        binding.screenSaver.visibility = View.GONE
        binding.imageViewLogoApp.visibility = View.GONE
    }

    private fun validLockTime() {
        when(accountStatus) {
            Constants.AccountStatus.ACCOUNT_CREATED.id -> {
                if (timeRequestAccessPin != -1) {
                    val currentTime = System.currentTimeMillis()

                    if(currentTime >= viewModel.getLockTimeApp()) {
                        viewModel.setLockStatus(Constants.LockStatus.LOCK.state)
                        navController.navigate(
                            R.id.enterPinFragment,
                            null,
                            NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                        )
                    }
                }
            }
        }
    }
}
