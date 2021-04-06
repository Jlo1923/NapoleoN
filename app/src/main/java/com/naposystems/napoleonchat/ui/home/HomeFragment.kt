package com.naposystems.napoleonchat.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.HomeFragmentBinding
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.home.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.home.adapter.FriendShipRequestReceivedAdapter
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_VERSION_CODE_KEY
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_VERSION_KEY
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.adapters.verifyPermission
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.napoleonchat.utility.showCaseManager.ShowCaseManager
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject


class HomeFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: HomeViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var handlerDialog: HandlerDialog


    //TODO:Subscription
    /*@Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle*/

    @Inject
    lateinit var webRTCClient: WebRTCClient


    private val shareContactViewModel: ShareContactViewModel by viewModels { viewModelFactory }

    private val shareFriendShipViewModel: FriendShipActionShareViewModel by viewModels { viewModelFactory }

    private val userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel by activityViewModels {
        viewModelFactory
    }

    private val timeFormatShareViewModel: TimeFormatShareViewModel by activityViewModels {
        viewModelFactory
    }

    private val contactRepositoryShareViewModel: ContactRepositoryShareViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: HomeFragmentBinding

    lateinit var conversationAdapter: ConversationAdapter

    private lateinit var friendShipRequestReceivedAdapter: FriendShipRequestReceivedAdapter

    private var existConversation: Boolean = false

    private var existFriendShip: Boolean = false

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }
    private lateinit var textViewBadge: TextView

    private var showCase: ShowCaseManager? = null
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    //    private lateinit var mFirebaseStorage: FirebaseStorage

    private var isShowingVersionDialog: Boolean = false

    private lateinit var popup: PopupMenu

    private var addContactsMenuItem: MenuItem? = null
    private var homeMenuItem: View? = null
    private var menuCreated: Boolean = false
    private var showShowCase: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO:Subscription
//        lifecycle.addObserver(billingClientLifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        //TODO: Verificar los mensajes no se estan borrando
        viewModel.verifyMessagesToDelete()

        //TODO:Subscription
//        billingClientLifecycle.queryPurchases()

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.home_fragment, container, false
        )

        setAdapter()

        setFriendshipRequest()

        binding.containerStatus.setOnClickListener {
            goToStatus()
        }

        //TODO:Subscription
        /*binding.containerSubscription.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSubscriptionFragment())
        }*/

        binding.imageButtonStatusEndIcon.setOnClickListener {
            goToStatus()
        }

        binding.fabContacts.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToContactsFragment()
            )
        }

        binding.buttonShowAllFriendship.setOnClickListener {
            goToAddContactFragment()
        }

        binding.textViewReturnCall.setOnClickListener {
            Timber.d("startCallActivity returnCall HomeFragment")
            val intent = Intent(context, ConversationCallActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putSerializable(ConversationCallActivity.KEY_CALL_MODEL, webRTCClient.callModel)
                    putBoolean(ConversationCallActivity.ITS_FROM_RETURN_CALL, true)
                })
            }
            startActivity(intent)
        }

        val disposableNewMessageReceived =
            RxBus.listen(RxEvent.NewFriendshipRequest::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.getFriendshipRequestHome()
                }


        val disposableCancelOrRejectFriendshipRequest =
            RxBus.listen(RxEvent.CancelOrRejectFriendshipRequestEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.getFriendshipRequestHome()
                }

        val disposableContactHasHangup = RxBus.listen(RxEvent.CallEnd::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.textViewReturnCall.isVisible = false
            }

        disposable.add(disposableNewMessageReceived)
        disposable.add(disposableCancelOrRejectFriendshipRequest)
        disposable.add(disposableContactHasHangup)

        binding.textViewStatus.isSelected = true


        /*if (showCase?.getStateShowCaseSixth() == true &&
            viewModel.getDialogSubscription() == Constants.ShowDialogSubscription.YES.option
        ) {
            generalDialog(
                "test",
                "message",
                false,
                childFragmentManager
            ) {
                viewModel.setDialogSubscription()
            }
        }*/

        RxBus.publish(RxEvent.HideOptionMenuRecoveryAccount())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contactRepositoryShareViewModel.getContacts(
            Constants.FriendShipState.ACTIVE.state,
            Constants.LocationGetContact.OTHER.location
        )

        viewModel.resetDuplicates()

        viewModel.getConversation()

        viewModel.getUserLiveData()

        viewModel.getMessages()

        viewModel.getDeletedMessages()

        viewModel.getFriendshipRequestHome()

        observeFriendshipRequestPutSuccessfully()

        observeFriendshipRequestWsError()

        observeFriendshipRequestAcceptedSuccessfully()

        timeFormatShareViewModel.getTimeFormat()

        viewModel.conversations?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                conversationAdapter.submitList(it)
                conversationAdapter.notifyDataSetChanged()
                existConversation = it.isNotEmpty()
                validateViewSwitcher(existConversation, existFriendShip)
                viewModel.resetConversations()
            }
        })

        viewModel.quantityFriendshipRequest.observe(viewLifecycleOwner, Observer {
            if (it != -1) setupBadge(it)
        })

        viewModel.friendShipRequestReceived.observe(viewLifecycleOwner, Observer {
            it?.let {
                friendShipRequestReceivedAdapter.submitList(it)
                binding.containerFriendRequestReceived.isVisible = it.isNotEmpty()
                existFriendShip = it.isNotEmpty()
                validateViewSwitcher(existConversation, existFriendShip)
            }
        })

        //TODO:Subscription
        /*billingClientLifecycle.purchases.observe(viewLifecycleOwner, Observer { purchasesList ->
            purchasesList?.let {
                for (purchase in purchasesList) {
                    billingClientLifecycle.acknowledged(purchase)
                }
                Timber.d("Billing purchases $purchasesList")
                billingClientLifecycle.queryPurchasesHistory()
            }
        })

        billingClientLifecycle.purchasesHistory.observe(
            viewLifecycleOwner,
            Observer { purchasesHistory ->
                purchasesHistory?.let {
                    Timber.d("Billing purchases $purchasesHistory")
                    val freeTrial = viewModel.getFreeTrial()
                    Timber.d("freeTrial: $freeTrial")
                    if (System.currentTimeMillis() > freeTrial) {
                        if (purchasesHistory.isNotEmpty()) {
                            try {
                                val dateExpireSubscriptionMillis =
                                    getDataSubscription(purchasesHistory)
                                if (System.currentTimeMillis() > dateExpireSubscriptionMillis) {
                                    binding.textViewMessageSubscription.text =
                                        getString(R.string.text_subscription_expired)
                                    binding.containerSubscription.isVisible = true
                                } else binding.containerSubscription.isVisible = false
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        } else {
                            binding.textViewMessageSubscription.text =
                                getString(R.string.text_free_trial_expired)
                            binding.containerSubscription.isVisible = true
                        }
                    } else binding.containerSubscription.isVisible = false
                }
            })*/

        viewModel.userEntity.observe(viewLifecycleOwner, Observer {
            binding.textViewStatus.text = it.status
        })

        viewModel.jsonNotification.observe(viewLifecycleOwner, Observer { json ->
            if (!json.isNullOrEmpty()) {
                viewModel.cleanJsonNotification(json)
            }
        })

        viewModel.jsonCleaned.observe(viewLifecycleOwner, Observer { json ->
            if (!json.isNullOrEmpty()) {
                val jsonNotification = JSONObject(json)
                when (jsonNotification.getInt(Constants.NotificationKeys.TYPE_NOTIFICATION)) {
                    Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                        viewModel.getContact(jsonNotification.getInt(Constants.NotificationKeys.CONTACT))
                    }
                    Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                        goToAddContactFragment()
                    }
                    Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToContactsFragment()
                        )
                    }
                }
            }
        })

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            it?.let { contact ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(contact)
                )
            }
        })

        (activity as MainActivity).getUser()
    }

    //TODO:Subscription
    /*private fun getDataSubscription(purchasesHistory: List<PurchaseHistoryRecord>): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchasesHistory[0].purchaseTime
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val lastPurchase = purchasesHistory[0]

        when (lastPurchase.sku) {
            Constants.SkuSubscriptions.MONTHLY.sku -> calendar.add(
                Calendar.MONTH,
                Constants.SubscriptionsTimeType.MONTHLY.subscription
            )

            Constants.SkuSubscriptions.SEMIANNUAL.sku -> calendar.add(
                Calendar.MONTH,
                Constants.SubscriptionsTimeType.SEMIANNUAL.subscription
            )

            else -> calendar.add(Calendar.YEAR, Constants.SubscriptionsTimeType.YEARLY.subscription)
        }

        val dateExpireSubscription = sdf.parse(sdf.format(calendar.time))
        return dateExpireSubscription!!.time
    }*/

    private fun validateViewSwitcher(existConversation: Boolean, existFriendShip: Boolean) {
        if (!existConversation && !existFriendShip && binding.viewSwitcherChats.nextView.id == binding.emptyState.id) {
            binding.viewSwitcherChats.showNext()
        } else if ((existConversation || existFriendShip) && binding.viewSwitcherChats.nextView.id == binding.containerContentHome.id) {
            binding.viewSwitcherChats.showNext()
        }
    }

    override fun onDetach() {
        viewModel.cleanVariables()
        super.onDetach()
    }

    override fun onPause() {
        showCase?.setPaused(true)
        showCase?.dismiss()
        showShowCase = false
        shareFriendShipViewModel.clearMessageError()
        viewModel.cleanVariables()
        if (::popup.isInitialized) {
            popup.dismiss()
        }
        super.onPause()
    }

    private fun goToAddContactFragment() {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToAddContactFragment(
                location = Constants.LocationAddContact.HOME.location
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_inbox, menu)

        val menuItem = menu.findItem(R.id.add_contact)
        addContactsMenuItem = menuItem

        val actionView = menuItem.actionView
        textViewBadge = actionView.findViewById(R.id.textView_badge)

        actionView.setOnClickListener {
            onOptionsItemSelected(menuItem)
        }

        homeMenuItem =
            ((activity as MainActivity).findViewById(R.id.toolbar) as MaterialToolbar).getChildAt(1)

        menuCreated = true

        showCase()

    }

    override fun onResume() {
        super.onResume()
        if (showCase?.getStateShowCaseSixth() == true) {
            this.verifyPermission(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                drawableIconId = R.drawable.ic_camera_primary,
                message = R.string.text_explanation_camera_to_receive_calls
            ) {
                //Intentionally empty
            }
        }

        showCase?.setPaused(false)
        viewModel.getJsonNotification()
        showCase()
        binding.textViewReturnCall.isVisible = webRTCClient.isActiveCall

        if (!isShowingVersionDialog && !BuildConfig.DEBUG) {
            Timber.d("*TestVersion: get remote")
            getRemoteConfig()
        }
    }

    private fun getRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getVersion()
                } else {
//                    this.showToast("No se han podido obtener el remote config|!!")
                    Timber.e("*TestVersion: Not remoteConfig")
                }
            }
    }

    private fun getVersion() {
        try {
            val versionApp = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_VERSION_KEY)
            val versionCodeApp = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_VERSION_CODE_KEY)

//            Timber.d("*TestVersion: $versionCodeApp")
//            Toast.makeText(context, "*TestVersion: ${versionCodeApp.toInt()}", Toast.LENGTH_SHORT).show()

            if (BuildConfig.VERSION_CODE < versionCodeApp.toInt()) {
                handlerDialog.alertDialogInformative(
                    title = getString(R.string.text_alert_failure),
                    message = getString(R.string.text_update_message, versionApp),
                    titleButton = R.string.text_update,
                    childFragmentManager = requireContext(),
                    clickTopButton = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(
                                "https://play.google.com/store/apps/details?id=com.naposystems.napoleonchat"
                            )
                            setPackage("com.android.vending")
                        }
                        startActivity(intent)
                        isShowingVersionDialog = false
                    },
                    isCancelable = false
                )
                isShowingVersionDialog = true
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    //TODO:Subscription
    /*private fun validateSubscriptionTime(purchaseHistoryList: List<PurchaseHistoryRecord>) {
        val freeTrial = viewModel.getFreeTrial()
        Timber.d("freeTrial: $freeTrial")

        if (System.currentTimeMillis() > freeTrial) {
            if (purchaseHistoryList.isEmpty()) {
                binding.textViewMessageSubscription.text =
                    getString(R.string.text_free_trial_expired)
                binding.containerSubscription.isVisible = true
            } else {
                if (System.currentTimeMillis() > purchaseHistoryList.last().purchaseTime) {
                    binding.textViewMessageSubscription.text =
                        getString(R.string.text_subscription_expired)
                    binding.containerSubscription.isVisible = true
                } else binding.containerSubscription.isVisible = false
            }
        } else {
            binding.containerSubscription.isVisible = false
        }
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_contact -> {
                goToAddContactFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBadge(friendshipRequestQuantity: Int) {
        if (::textViewBadge.isInitialized) {
            if (friendshipRequestQuantity > 0) {
                textViewBadge.visibility = View.VISIBLE
                textViewBadge.text = friendshipRequestQuantity.toString()
            } else {
                textViewBadge.visibility = View.GONE
            }
        }
    }

    private fun goToStatus() {
        viewModel.userEntity.value?.let { user ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToStatusFragment(user)
            )
        }
    }

    private fun setAdapter() {
        conversationAdapter = ConversationAdapter(
            object : ConversationAdapter.ClickListener {
                override fun onClick(item: MessageAttachmentRelation) {
                    item.contact?.let { contact ->
                        findNavController().currentDestination?.getAction(R.id.action_homeFragment_to_conversationFragment)
                            ?.let {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(
                                        contact
                                    )
                                )
                            }
                    }
                }

                override fun onClickAvatar(item: MessageAttachmentRelation) {
                    item.contact?.let { contact ->
                        seeProfile(contact)
                    }

                }

                override fun onLongClick(item: MessageAttachmentRelation, view: View) {
                    item.contact?.let { contact ->
                        popup = PopupMenu(context!!, view)
                        popup.menuInflater.inflate(R.menu.menu_inbox_conversation, popup.menu)

                        popup.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.start_chat_from_inbox ->
                                    startConversation(contact)
                                R.id.see_profile_from_inbox ->
                                    seeProfile(contact)
                                R.id.delete_chat_from_inbox ->
                                    deleteChat(contact)
                                R.id.block_contact_from_inbox ->
                                    blockContact(contact)
                            }
                            true
                        }
                        popup.show()
                    }
                }
            },
            userDisplayFormatShareViewModel.getUserDisplayFormat(),
            timeFormatShareViewModel.getValTimeFormat()
        )
        binding.recyclerViewChats.apply {
            adapter = conversationAdapter
            isNestedScrollingEnabled = false
            itemAnimator = ItemAnimator()
        }
    }

    private fun startConversation(contact: ContactEntity) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToConversationFragment(contact)
        )
    }

    private fun seeProfile(contact: ContactEntity) {
        findNavController().currentDestination?.getAction(R.id.action_homeFragment_to_contactProfileFragment)
            ?.let {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToContactProfileFragment(contact.id)
                )
            }
    }

    private fun observeFriendshipRequestAcceptedSuccessfully() {
        shareFriendShipViewModel.friendshipRequestAcceptedSuccessfully.observe(
            viewLifecycleOwner,
            Observer {
                if (it == true) {
                    viewModel.getFriendshipRequestHome()
                }
            })
    }

    private fun observeFriendshipRequestWsError() {
        shareFriendShipViewModel.friendshipRequestWsError.observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {

                val list = ArrayList<String>()
                list.add(it)

                val snackbarUtils = SnackbarUtils(binding.coordinator, list)

                snackbarUtils.showSnackbar { ok ->
                    if (ok)
                        shareFriendShipViewModel.clearMessageError()
                }
            }
        })
    }

    private fun observeFriendshipRequestPutSuccessfully() {
        shareFriendShipViewModel.friendshipRequestPutSuccessfully.observe(
            viewLifecycleOwner,
            Observer {
                if (it == true) {
                    viewModel.getFriendshipRequestHome()
                }
            })
    }

    private fun setFriendshipRequest() {
        friendShipRequestReceivedAdapter = FriendShipRequestReceivedAdapter(object :
            FriendShipRequestReceivedAdapter.ClickListener {
            override fun onRefuse(friendshipRequest: FriendShipRequest) {
                shareFriendShipViewModel.refuseFriendshipRequest(friendshipRequest)
            }

            override fun onAccept(friendshipRequest: FriendShipRequest) {
                shareFriendShipViewModel.acceptFriendshipRequest(friendshipRequest)
            }
        })

        binding.recyclerViewFriendshipRequest.adapter = friendShipRequestReceivedAdapter
    }

    private fun deleteChat(contact: ContactEntity) {
        handlerDialog.generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.deleteConversation(contact.id)
        }
    }

    private fun blockContact(contact: ContactEntity) {
        handlerDialog.generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(contact)
        }
    }

    private fun showCase() {
        try {
            Handler().postDelayed({
                if (menuCreated && !showShowCase) {
                    val drawerMenu = (requireActivity() as MainActivity).getNavView().menu

                    val securitySettingMenuItem =
                        drawerMenu.findItem(R.id.security_settings).actionView as LinearLayout

                    showCase = ShowCaseManager().apply {
                        setListener(object : ShowCaseManager.Listener {
                            override fun openSecuritySettings() {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeFragmentToSecuritySettingsFragment(
                                        showShowCase = true
                                    )
                                )
                            }
                        })

                        setActivity(requireContext() as FragmentActivity)
                        addContactsMenuItem?.actionView?.let { view ->
                            setFirstView(view)
                        }
                        setSecondView(binding.fabContacts)
                        setThirdView(binding.viewShowCaseStatus)
                        setFourthView(homeMenuItem!!)
                        setFifthView(securitySettingMenuItem.getChildAt(0))

                        showFromFirst()
                    }

                    showShowCase = true
                }
            }, 500)
        } catch (e: Exception) {
            Timber.d(e.localizedMessage)
        }
    }
}
