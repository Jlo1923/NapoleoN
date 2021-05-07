package com.naposystems.napoleonchat.ui.mainActivity

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Display
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityMainBinding
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.ui.accountAttack.AccountAttackDialogFragment
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.LocaleHelper
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.hasMicAndCameraPermission
import com.naposystems.napoleonchat.utility.extensions.*
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var notificationClient: NotificationClient

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var viewModel: MainActivityViewModel

    private val contactSharedViewModel: ContactSharedViewModel by viewModels { viewModelFactory }

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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        when (sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)) {
            Constants.ThemesApplication.LIGHT_NAPOLEON.theme -> setTheme(R.style.AppTheme)
            Constants.ThemesApplication.DARK_NAPOLEON.theme -> setTheme(R.style.AppThemeDarkNapoleon)
            Constants.ThemesApplication.BLACK_GOLD_ALLOY.theme -> setTheme(R.style.AppThemeBlackGoldAlloy)
            Constants.ThemesApplication.COLD_OCEAN.theme -> setTheme(R.style.AppThemeColdOcean)
            Constants.ThemesApplication.CAMOUFLAGE.theme -> setTheme(R.style.AppThemeCamouflage)
            Constants.ThemesApplication.PURPLE_BLUEBONNETS.theme -> setTheme(R.style.AppThemePurpleBluebonnets)
            Constants.ThemesApplication.PINK_DREAM.theme -> setTheme(R.style.AppThemePinkDream)
            Constants.ThemesApplication.CLEAR_SKY.theme -> setTheme(R.style.AppThemeClearSky)
        }

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainActivityViewModel::class.java)

        intent.extras?.let { bundle ->

            var callModel = CallModel(
                contactId = 0,
                channelName = "",
                isVideoCall = false
            )

            if (bundle.containsKey(Constants.CallKeys.CALL_MODEL)) {
                callModel = bundle.getSerializable(Constants.CallKeys.CALL_MODEL) as CallModel
            }

            if (callModel.channelName != "" || callModel.contactId > 0) {
                viewModel.setCallChannel(callModel.channelName)
                viewModel.setIsVideoCall(callModel.isVideoCall)
                viewModel.getContact(callModel.contactId)
            }
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        viewModel.getAccountStatus()

        viewModel.accountStatus.observe(this, Observer {
            accountStatus = it
        })

        WebView(applicationContext)

        val language = LocaleHelper.getLanguagePreference(this)
        LocaleHelper.updateResources(this, language)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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

        val disposableIncomingCall = RxBus.listen(RxEvent.IncomingCall::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (this.hasMicAndCameraPermission()) {

                    Timber.d("LLAMADA PASO: INICIANDO CONVERSATIONCALLACTIVITY")

                    val intent = Intent(
                        applicationContext,
                        ConversationCallActivity::class.java
                    ).apply {
                        putExtras(Bundle().apply {
                            it.callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL
                        })
                    }
                    startActivity(intent)
                }
            }

        disposable.add(disposableIncomingCall)

        val disposableFriendRequestAccepted =
            RxBus.listen(RxEvent.FriendshipRequestAccepted::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    contactSharedViewModel.getContacts(
                        Constants.FriendShipState.ACTIVE.state,
                        Constants.LocationGetContact.OTHER.location
                    )
                }
        disposable.add(disposableFriendRequestAccepted)

        val disposableDeleteChannel =
            RxBus.listen(RxEvent.DeleteChannel::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("*TestDelete: State ${it.contact.stateNotification}")
                    if (it.contact.stateNotification) {
                        Timber.d("*TestDelete: Contact ${it.contact.id}")
                        Timber.d("*TestDelete: Contact ${it.contact.getNickName()}")
                        handlerNotificationChannel.deleteUserChannel(
                            it.contact.id,
                            it.contact.getNickName(),
                            it.contact.notificationId
                        )
                    }
                }

        disposable.add(disposableDeleteChannel)

        val disposableHideOptionMenuRecoveryAccount =
            RxBus.listen(RxEvent.HideOptionMenuRecoveryAccount::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideOptionMenuRecoveryAccount()
                }

        disposable.add(disposableHideOptionMenuRecoveryAccount)


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
                    showToolbar()
                    disableDrawer()
                    dontOpenMenu()
                    binding.toolbar.setContentInsetsAbsolute(0, 0)
                    binding.toolbar.elevation = 0f
                    binding.frameLayout.elevation = 0f
                }
                R.id.accessPinFragment -> {

                    Timber.d("AccountStatus Destination Listener {$accountStatus}")

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
                    resetToolbar()
                    supportActionBar?.subtitle = getString(R.string.text_tap_to_select)
                    disableDrawer()
                }
                else -> {
                    showToolbar()
                    resetToolbar()
                    dontOpenMenu()
                    disableDrawer()
                }
            }
        }

        viewModel.userEntity.observe(this, Observer {
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

        viewModel.contact.observe(this, Observer { contact ->
            if (contact != null && this.hasMicAndCameraPermission()) {
                Timber.d("startCallActivity MainActivity viewmodel.contact")
                val intent = Intent(this, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
//                        putSerializable(
//                            ConversationCallActivity.KEY_CALL_MODEL, CallModel(
//                                contactId = contact.id,
//                                channelName = viewModel.getCallChannel(),
//                                isVideoCall = viewModel.isVideoCall() ?: false,
//                                typeCall = Constants.TypeCall.IS_INCOMING_CALL
//                            )
//                        )
                    })
                }
                startActivity(intent)

                viewModel.resetContact()
                viewModel.resetCallChannel()
            }
        })

        setupNotifications(intent)

        binding.navView.setNavigationItemSelectedListener(this)

        setMarginToNavigationView()

        hideOptionMenuForAndroidVersion()

        hideOptionMenuRecoveryAccount()

        validateExtrasForShareFromOutside()
    }

    private fun openMenu() {
        binding.toolbar.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setupNotifications(intent)
    }

    private fun setupNotifications(intent: Intent?) {
        intent?.extras?.let { args ->
            if (args.containsKey(Constants.NotificationKeys.TYPE_NOTIFICATION)) {
                val jsonNotification = JSONObject()
                jsonNotification.put(
                    Constants.NotificationKeys.TYPE_NOTIFICATION,
                    args.getString(Constants.NotificationKeys.TYPE_NOTIFICATION)?.toInt()!!
                )
                if (args.getString(Constants.NotificationKeys.TYPE_NOTIFICATION)
                        ?.toInt() == Constants.NotificationType.ENCRYPTED_MESSAGE.type
                ) {
                    jsonNotification.put(
                        Constants.NotificationKeys.CONTACT,
                        args.getString(Constants.NotificationKeys.CONTACT)?.toInt()!!
                    )
                    jsonNotification.put(
                        Constants.NotificationKeys.MESSAGE_ID,
                        args.getString(Constants.NotificationKeys.MESSAGE_ID)
                    )
                }
                viewModel.setJsonNotification(jsonNotification.toString())
            }
            if (args.containsKey(Constants.NotificationKeys.ATTACKER_ID)) {
                val dialog = AccountAttackDialogFragment()
                dialog.show(supportFragmentManager, "AttackDialog")
            }
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

    private fun updateHeaderDrawer(userEntity: UserEntity) {
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

        val imageUrl = userEntity.imageUrl

        Glide.with(this)
            .load(
                if (imageUrl.isEmpty()) defaultAvatar else imageUrl
            )
            .circleCrop()
            .into(imageViewAvatar)

        Glide.with(this)
            .load(
                if (userEntity.headerUri.isEmpty()) {
                    defaultHeaderBackground
                } else {
                    Utils.getFileUri(
                        context = this,
                        fileName = userEntity.headerUri,
                        subFolder = Constants.CacheDirectories.HEADER.folder
                    )
                }
            )
            .into(imageViewBackground)

        if (userEntity.displayName != "") {
            textViewDisplayName.visibility = View.VISIBLE
        } else {
            textViewDisplayName.visibility = View.GONE
        }

        textViewDisplayName.text = userEntity.displayName

        val nickname = getString(R.string.label_nickname, userEntity.nickname)

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
        supportActionBar?.let { actionBar ->
            with(actionBar) {
                subtitle = ""
                setDisplayShowCustomEnabled(false)
                setDisplayShowTitleEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }
    }

    private fun enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun getNavController() = this.navController

    fun getNavView() = findViewById<NavigationView>(R.id.nav_view)

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
        applyOverrideConfiguration(newBase?.resources?.configuration)
    }

    override fun onBackPressed() {
        Timber.d("startCallActivity onBackPressed")
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
            //TODO:Subscription
            /*R.id.subscription -> navController.navigate(
                R.id.subscriptionFragment,
                null,
                options
            )*/
            R.id.security_settings -> navController.navigate(
                R.id.securitySettingsFragment,
                null,
                options
            )
            R.id.recovery_account_option_main_menu -> navController.navigate(
                R.id.registerRecoveryAccountFragment,
                null,
                options
            )
            R.id.appearance_settings -> navController.navigate(
                R.id.appearanceSettingsFragment,
                null,
                options
            )
            R.id.notification_option_main_menu -> navController.navigate(
                R.id.notificationFragment,
                null,
                options
            )
            R.id.invite_someone -> navController.navigate(R.id.inviteSomeoneFragment, null, options)
            R.id.help -> navController.navigate(R.id.helpFragment, null, options)
        }

        return true
    }

    /*override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }*/

    private fun hideOptionMenuForAndroidVersion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.navView.menu.findItem(R.id.notification_option_main_menu).isVisible = false
        }
    }

    private fun hideOptionMenuRecoveryAccount() {
        if (viewModel.getRecoveryQuestionsPref() == Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id) {
            binding.navView.menu.findItem(R.id.recovery_account_option_main_menu).isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        if (viewModel.getOutputControl() == Constants.OutputControl.TRUE.state) {
            viewModel.setOutputControl(Constants.OutputControl.FALSE.state)
        } else {
            validLockTime()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setLockTimeApp()
        viewModel.disconnectSocket()
    }

    override fun onDestroy() {
        disposable.clear()
        viewModel.disconnectSocket()
        super.onDestroy()
    }

    private fun validLockTime() {
        if (viewModel.getOutputControl() == Constants.OutputControl.FALSE.state) {
            when (accountStatus) {
                Constants.AccountStatus.ACCOUNT_CREATED.id -> {

                    Timber.d("AccountStatus validLockTime {$accountStatus}")

                    val timeAccessRequestPin = viewModel.getTimeRequestAccessPin()
                    if (timeAccessRequestPin != Constants.TimeRequestAccessPin.NEVER.time) {
                        val currentTime = System.currentTimeMillis()

                        if (currentTime >= viewModel.getLockTimeApp()) {
                            navToEnterPin()
                        }
                    }
                }
            }
        }
    }

    private fun validateExtrasForShareFromOutside() = intent.apply {
        when {
            isActionSend() -> handleSingleSend()
            isActionSendMultiple() -> handleMultipleSend()
            else -> Unit // Handle other intents, such as being started from the home screen
        }
    }

    private fun handleSingleSend() {
        intent.apply {
            if (isTypeVideoOrImage()) {
                (getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    viewModel.addUriListToCache(listOf(it))
                    navToEnterPin()
                }
            }
        }
    }

    private fun handleMultipleSend() {
        intent.apply {
            if (isTypeAnyOrVideoOrImage()) {
                val urisList = getUriListFromExtra()
                if (urisList.size > 10) {
                    viewModel.removeUriListCache()
                    Utils.showToast(
                        binding.root.context,
                        getString(R.string.multi_max_files_from_outside)
                    )
                    finish()
                } else {
                    viewModel.addUriListToCache(urisList)
                    navToEnterPin()
                }
            }
        }
    }

    private fun navToEnterPin() {
        viewModel.setLockStatus(Constants.LockStatus.LOCK.state)
        navController.navigate(
            R.id.enterPinFragment,
            null,
            NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
        )
    }

}