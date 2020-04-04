package com.naposystems.pepito.ui.mainActivity

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.accountAttack.AccountAttackDialogFragment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: MainActivityViewModel
    private var accountStatus: Int = 0
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

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

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainActivityViewModel::class.java)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        viewModel.getAccountStatus()
        viewModel.accountStatus.observe(this, Observer {
            accountStatus = it
        })

        when (sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)) {
            1 -> setNewTheme(Constants.ColorScheme.LIGHT_THEME.scheme)
            2 -> setNewTheme(Constants.ColorScheme.DARK_THEME.scheme)
            3 -> setNewTheme(R.style.AppThemeBlackGoldAlloy)
            4 -> setNewTheme(R.style.AppThemeColdOcean)
            5 -> setNewTheme(R.style.AppThemeCamouflage)
            6 -> setNewTheme(R.style.AppThemePurpleBluebonnets)
            7 -> setNewTheme(R.style.AppThemePinkDream)
            8 -> setNewTheme(R.style.AppThemeClearSky)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val disposableNoInternetConnection = RxBus.listen(RxEvent.NoInternetConnection::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(
                    this, getString(R.string.text_error_connection), Toast.LENGTH_SHORT
                ).show()
            }

        disposable.add(disposableNoInternetConnection)

        val disposableAccountAttack = RxBus.listen(RxEvent.AccountAttack::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val dialog = AccountAttackDialogFragment()

                dialog.show(supportFragmentManager, "AttackDialog")
            }

        disposable.add(disposableAccountAttack)

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
            viewModel.getAccountStatus()
            Utils.hideKeyboard(binding.coordinator)
            when (destination.id) {
                R.id.splashFragment,
                R.id.landingFragment,
                R.id.registerFragment,
                R.id.enterPinFragment,
                R.id.unlockAppTimeFragment,
                R.id.conversationCameraFragment,
                R.id.attachmentPreviewFragment,
                R.id.previewMediaFragment -> {
                    hideToolbar()
                    disableDrawer()
                }
                R.id.homeFragment -> {
                    showToolbar()
                    enableDrawer()
                    openMenu()
                }
                R.id.conversationFragment -> {
                    disableDrawer()
                    showToolbar()
                    dontOpenMenu()
                    binding.toolbar.setContentInsetsAbsolute(0, 0)
                    binding.toolbar.elevation = 0f
                    binding.frameLayout.elevation = 0f
                    /*binding.toolbar.setBackgroundColor(
                        resources.getColor(
                            R.color.flatActionBarColor,
                            this.theme
                        )
                    )*/
                }
                R.id.accessPinFragment -> {
                    if (accountStatus == Constants.AccountStatus.ACCOUNT_RECOVERED.id) {
                        hideToolbar()
                        disableDrawer()
                    } else {
                        showToolbar()
                        dontOpenMenu()
                    }
                }
                R.id.attachmentAudioFragment -> {
                    showToolbar()
                    supportActionBar?.subtitle = getString(R.string.text_tap_to_select)
                    disableDrawer()
                }
                else -> {
                    showToolbar()
                    dontOpenMenu()
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

        intent.extras?.let { args ->
            if (args.containsKey(Constants.TYPE_NOTIFICATION)) {
                val jsonNotification = JSONObject()
                jsonNotification.put(Constants.TYPE_NOTIFICATION, args.getString(Constants.TYPE_NOTIFICATION)?.toInt()!!)
                if(args.getString(Constants.TYPE_NOTIFICATION)?.toInt() == Constants.NotificationType.ENCRYPTED_MESSAGE.type){
                    jsonNotification.put(Constants.TYPE_NOTIFICATION_WITH_CONTACT, args.getString(Constants.TYPE_NOTIFICATION_WITH_CONTACT)?.toInt()!!)
                }
                viewModel.setJsonNotification(jsonNotification.toString())
            }
        }

        binding.navView.setNavigationItemSelectedListener(this)

        setMarginToNavigationView()
    }

    private fun setNewTheme(style: Int) {
        if (style != Constants.ColorScheme.DARK_THEME.scheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setTheme(style)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun openMenu() {
        binding.toolbar.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun dontOpenMenu() {
        binding.toolbar.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
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
            .load(if (user.headerUri.isEmpty()) {
                defaultHeaderBackground
            } else {
                Utils.getFileUri(
                    context = this,
                    fileName = user.headerUri,
                    subFolder = Constants.NapoleonCacheDirectories.HEADER.folder
                )
            })
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
        resetToolbar()

        val value = TypedValue()
        this.theme.resolveAttribute(R.attr.attrBackgroundTintIconToolbar, value, true)

        val fourDp = Utils.dpToPx(this, 4f).toFloat()
        binding.toolbar.apply {
            overflowIcon?.setColorFilter(
                ContextCompat.getColor(context, value.resourceId), PorterDuff.Mode.SRC_IN
            )
            visibility = View.VISIBLE
            elevation = fourDp
        }
        binding.frameLayout.elevation = fourDp
    }

    private fun resetToolbar() {
        with(supportActionBar!!) {
            subtitle = ""
            setDisplayShowCustomEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun getNavController() = this.navController

    fun changeLayoutHeight(height: Int) {
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val layoutParams = binding.root.layoutParams
        layoutParams.height = size.y - height
        binding.root.layoutParams = layoutParams
    }

    fun resetLayoutHeight() {
        val layoutParams = binding.root.layoutParams
        layoutParams.height = MATCH_PARENT
        binding.root.layoutParams = layoutParams
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
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            binding.searchView.isOpened() -> {
                binding.searchView.showSearchView()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        binding.drawerLayout.closeDrawers()

        when (menuItem.itemId) {
            R.id.suscription -> navController.navigate(
                R.id.subscriptionFragment,
                null,
                options
            )
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
        if (viewModel.getOutputControl() == Constants.OutputControl.TRUE.state) {
            viewModel.setOutputControl(Constants.OutputControl.FALSE.state)
        } else {
            validLockTime()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setLockTimeApp()
    }

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    private fun validLockTime() {
        if (viewModel.getOutputControl() == Constants.OutputControl.FALSE.state) {
            when (accountStatus) {
                Constants.AccountStatus.ACCOUNT_CREATED.id -> {
                    val timeAccessRequestPin = viewModel.getTimeRequestAccessPin()
                    if (timeAccessRequestPin != Constants.TimeRequestAccessPin.NEVER.time) {
                        val currentTime = System.currentTimeMillis()

                        if (currentTime >= viewModel.getLockTimeApp()) {
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
}
