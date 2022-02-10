package com.naposystems.napoleonchat.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
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
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.databinding.HomeFragmentBinding
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogViewModel
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogViewModel
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.contacts.ContactsFragmentDirections
import com.naposystems.napoleonchat.ui.contacts.adapter.ContactsAdapter
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.home.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.home.adapter.FriendShipRequestReceivedAdapter
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_VERSION_CODE_KEY
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_VERSION_KEY
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_USER_ID
import com.naposystems.napoleonchat.utility.adapters.verifyPermission
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionSharedViewModel
import com.naposystems.napoleonchat.utility.showCaseManager.ShowCaseManager
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment(), SearchView.OnSearchView {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var handlerDialog: HandlerDialog

    private val homeViewModel: HomeViewModel by viewModels { viewModelFactory }

    //TODO:Subscription
    /*@Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle*/

    //    private lateinit var mFirebaseStorage: FirebaseStorage

    private val contactSharedViewModel: ContactSharedViewModel by viewModels { viewModelFactory }

    private val shareFriendShipViewModel: FriendShipActionSharedViewModel by viewModels { viewModelFactory }

    private val userDisplayFormatDialogViewModel: UserDisplayFormatDialogViewModel by activityViewModels {
        viewModelFactory
    }

    private val timeFormatShareViewModel: TimeFormatDialogViewModel by activityViewModels {
        viewModelFactory
    }

    private var totalBlockDialog: DialogFragment? = null

    private lateinit var binding: HomeFragmentBinding

    lateinit var conversationAdapter: ConversationAdapter

    lateinit var contactsAdapter: ContactsAdapter

    private lateinit var friendShipRequestReceivedAdapter: FriendShipRequestReceivedAdapter

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private lateinit var textViewBadge: TextView
    private lateinit var imageViewSearch: TextView

    private var showCaseManager: ShowCaseManager? = null

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private lateinit var popup: PopupMenu

    private var addContactsMenuItem: MenuItem? = null
    private var searchAllMenuItem: MenuItem? = null

    private var homeMenuItem: View? = null

    private var existConversation: Boolean = false

    private var existFriendShip: Boolean = false

    private var isShowingVersionDialog: Boolean = false

    private var isMenuCreated: Boolean = false

    private var isShowingShowCase: Boolean = false

    private lateinit var mainActivity: MainActivity

    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.lastSubscription()
        //TODO:Subscription
//        lifecycle.addObserver(billingClientLifecycle)
        subscriptionStatus = SubscriptionStatus.valueOf(
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.SubscriptionStatus,
                SubscriptionStatus.ACTIVE.name
            )
        )
        validateMustGoToContacts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        //TODO: Verificar los mensajes no se estan borrando
        homeViewModel.verifyMessagesToDelete()

        homeViewModel.verifyMessagesReceived()

        homeViewModel.verifyMessagesRead()

        //TODO:Subscription
//        billingClientLifecycle.queryPurchases()

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.home_fragment, container, false
        )

        setAdapter()

        setAdapterContacts()

        setFriendshipRequest()

        binding.containerStatus.setOnClickListener {
            goToStatus()
        }

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
                    putBoolean(ConversationCallActivity.ACTION_RETURN_CALL, true)
                })
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val disposableNewMessageReceived =
            RxBus.listen(RxEvent.NewFriendshipRequest::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    homeViewModel.getFriendshipRequestHome()
                }

        val disposableCancelOrRejectFriendshipRequest =
            RxBus.listen(RxEvent.CancelOrRejectFriendshipRequestEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    homeViewModel.getFriendshipRequestHome()
                }

        val disposableContactHasHangup = RxBus.listen(RxEvent.CallEnd::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("LLAMADA PASO: DISPOSE CALL OYENDO DESDE HOME")
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

        this.verifyPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            drawableIconId = R.drawable.ic_camera_primary,
            message = R.string.text_explanation_camera_to_receive_calls
        ) {
            //Intentionally empty
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contactSharedViewModel.getContacts(
            Constants.FriendShipState.ACTIVE.state,
            Constants.LocationGetContact.OTHER.location
        )

        timeFormatShareViewModel.getTimeFormat()

        homeViewModel.resetDuplicates()

        homeViewModel.getConversation()

        homeViewModel.getUserLiveData()

        homeViewModel.getMessages()

        homeViewModel.getDeletedMessages()

        homeViewModel.getFriendshipRequestHome()

        homeViewModel.getLocalContacts()

        observeFriendshipRequestPutSuccessfully()

        observeFriendshipRequestWsError()

        observeFriendshipRequestAcceptedSuccessfully()

        observeConversations()

        observeContacts()

        observeQuantityFriendshipRequest()

        observeFriendshipRequestReceived()

        observeUser()

        observeJsonNotification()

        observeJsonCleaned()

        observeContact()

        observeSubscription()

        observeContactsForSearch()

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

        (activity as MainActivity).getUser()
    }

    private fun validateMustGoToContacts() {
        val mustGoToContacts = homeViewModel.isMarkGoToContacts() &&
                (subscriptionStatus == SubscriptionStatus.ACTIVE ||
                        subscriptionStatus == SubscriptionStatus.FREE_TRIAL ||
                        subscriptionStatus == SubscriptionStatus.FREE_TRIAL_DAY_4)
        if (mustGoToContacts) {
            /**
             * Solo podemos ir a contactos una sola vez, si se devuelve ya se pierden los archivos
             * seleccionados previamente desde afuera
             */
            homeViewModel.markGoToContacts()
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToContactsFragment()
            )
        }
    }

    private fun observeContact() {
        homeViewModel.contact.observe(viewLifecycleOwner, Observer {
            it?.let { contact ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(contact)
                )
            }
        })
    }

    private fun observeJsonCleaned() {
        homeViewModel.jsonCleaned.observe(viewLifecycleOwner, Observer { json ->
            if (!json.isNullOrEmpty()) {
                val jsonNotification = JSONObject(json)
                when (jsonNotification.getInt(Constants.NotificationKeys.TYPE_NOTIFICATION)) {
                    Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                        homeViewModel.getContact(jsonNotification.getInt(Constants.NotificationKeys.CONTACT))
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
    }

    private fun observeJsonNotification() {
        homeViewModel.jsonNotification.observe(viewLifecycleOwner, Observer { json ->
            if (!json.isNullOrEmpty()) {
                homeViewModel.cleanJsonNotification(json)
            }
        })
    }

    private fun observeUser() {
        homeViewModel.userEntity.observe(viewLifecycleOwner, Observer {
            binding.textViewStatus.text = it.status
        })
    }

    private fun observeFriendshipRequestReceived() {
        homeViewModel.friendShipRequestReceived.observe(viewLifecycleOwner, Observer {
            it?.let {
                friendShipRequestReceivedAdapter.submitList(it)
                binding.containerFriendRequestReceived.isVisible = it.isNotEmpty()
                existFriendShip = it.isNotEmpty()
                validateViewSwitcher(existConversation, existFriendShip)
            }
        })
    }

    private fun observeQuantityFriendshipRequest() {
        homeViewModel.quantityFriendshipRequest.observe(viewLifecycleOwner, Observer {
            if (it != -1) setupBadge(it)
        })
    }

    private fun observeSubscription() {
        val disposableSubscriptionStatus = RxBus.listen(RxEvent.SubscriptionStatusEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                subscriptionStatus = it.status
                setupSubscriptionContainer(it.status)
            }
        disposable.add(disposableSubscriptionStatus)
    }

    private fun setupSubscriptionContainer(status: SubscriptionStatus) {
        binding.containerSubscription.setOnClickListener {
            subscriptionIntent()
        }
        when (status) {
            SubscriptionStatus.FREE_TRIAL -> binding.containerSubscription.isVisible = false
            SubscriptionStatus.FREE_TRIAL_DAY_4 -> {
                binding.containerSubscription.isVisible = true
                binding.textViewMessageSubscription.setText(R.string.text_subscription_free_trial_fourth_day)
                binding.textViewMessageSubscription.isVisible = true
            }
            SubscriptionStatus.PARTIAL_LOCK -> {
                binding.containerSubscription.isVisible = true
                binding.textViewMessageSubscription.setText(R.string.text_subscription_partial_lock)
                binding.textViewMessageSubscription.isVisible = true
                binding.containerStatus.isClickable = false
                binding.containerStatus.isEnabled = false
                binding.imageButtonStatusEndIcon.isClickable = false
                binding.imageButtonStatusEndIcon.isEnabled = false
            }
            SubscriptionStatus.TOTAL_LOCK -> {
                createTotalBlockDialog()
            }
            SubscriptionStatus.ACTIVE -> binding.containerSubscription.isVisible = false
        }
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
        homeViewModel.cleanVariables()
        super.onDetach()
    }

    override fun onPause() {
        showCaseManager?.setPaused(true)
        showCaseManager?.dismiss()
        isShowingShowCase = false
        shareFriendShipViewModel.clearMessageError()
        homeViewModel.cleanVariables()
        searchView.close()
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



        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setStyleable(Constants.LocationSearchView.OTHER.location)
            searchView.setHint(R.string.text_search_contact)
            searchView.setMenuItem(menu.findItem(R.id.search_contacts))
            searchView.setListener(this)
        }

        isMenuCreated = true
        showCase()

    }

    override fun onResume() {
        super.onResume()
        if (showCaseManager?.getStateShowCaseSixth() == true) {
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

        showCaseManager?.setPaused(false)
        homeViewModel.getJsonNotification()
        showCase()
        binding.textViewReturnCall.isVisible = NapoleonApplication.statusCall.isConnectedCall()

        if (!isShowingVersionDialog && !BuildConfig.DEBUG) {
            Timber.d("*TestVersion: get remote")
            getRemoteConfig()
        }
    }

    private fun getRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.fetchAndActivate()
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
            val versionApp = firebaseRemoteConfig.getString(REMOTE_CONFIG_VERSION_KEY)
            val versionCodeApp = firebaseRemoteConfig.getString(REMOTE_CONFIG_VERSION_CODE_KEY)

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
            else -> false
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
        homeViewModel.userEntity.value?.let { user ->
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
            userDisplayFormatDialogViewModel.getUserDisplayFormat(),
            timeFormatShareViewModel.getValTimeFormat()
        )
        binding.recyclerViewChats.apply {
            adapter = conversationAdapter
            isNestedScrollingEnabled = false
            itemAnimator = ItemAnimator()
        }
    }


    private fun setAdapterContacts() {
        contactsAdapter = ContactsAdapter(object : ContactsAdapter.ContactClickListener {
            override fun onClick(item: ContactEntity) {
                if (item.id != 0) {
                    startConversation(item)
                }
            }

            override fun onMoreClick(item: ContactEntity, view: View) {

            }
        }, userDisplayFormatDialogViewModel)
        binding.recyclerViewContacts.adapter = contactsAdapter
        binding.recyclerViewContacts.itemAnimator = ItemAnimator()

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

    private fun observeConversations() {
        homeViewModel.conversations?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                conversationAdapter.submitList(it)
                conversationAdapter.notifyDataSetChanged()
                existConversation = it.isNotEmpty()
                validateViewSwitcher(existConversation, existFriendShip)
                homeViewModel.resetConversations()
            }
        })
    }

    private fun observeContacts() {
        homeViewModel.contacts.observe(viewLifecycleOwner, Observer { listContacts ->
            if (listContacts != null) {
                if (listContacts.count() >= 1) {

                    listContacts.sortBy { contact ->
                        contact.getNickName()
                    }
                }
                contactsAdapter.submitList(listContacts)
            }
        })
    }

    private fun observeContactsForSearch() {
        homeViewModel.contactsForSearch?.observe(viewLifecycleOwner, Observer {
            if(it != null){
                contactsAdapter.submitList(it)
            }
        })
    }

    private fun observeFriendshipRequestAcceptedSuccessfully() {
        shareFriendShipViewModel.friendshipRequestAcceptedSuccessfully.observe(
            viewLifecycleOwner,
            Observer {
                if (it == true) {
                    homeViewModel.getFriendshipRequestHome()
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
                    homeViewModel.getFriendshipRequestHome()
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
            contactSharedViewModel.deleteConversation(contact.id)
        }
    }

    private fun blockContact(contact: ContactEntity) {
        handlerDialog.generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            contactSharedViewModel.sendBlockedContact(contact)
        }
    }

    private fun showCase() {
        try {

            Handler().postDelayed({
                if (isMenuCreated && !isShowingShowCase) {
                    val drawerMenu = (requireActivity() as MainActivity).getNavView().menu

                    val securitySettingMenuItem =
                        drawerMenu.findItem(R.id.security_settings).actionView as LinearLayout

                    showCaseManager = ShowCaseManager().apply {
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

                    isShowingShowCase = true
                }
            }, 500)
        } catch (e: Exception) {
            Timber.d(e.localizedMessage)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSubscriptionContainer(subscriptionStatus)
    }

    private fun subscriptionIntent() {
        val userId = sharedPreferencesManager.getString(PREF_USER_ID, "")
        val url = getString(R.string.buy_subscription_url).plus(userId)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun createTotalBlockDialog() {
        if (totalBlockDialog == null) {
            totalBlockDialog = handlerDialog.generalDialog(
                resources.getString(R.string.text_total_lock_dialog_title),
                resources.getString(R.string.text_total_lock_dialog_desc),
                false,
                childFragmentManager,
                textButtonAccept = resources.getString(R.string.text_total_lock_dialog_action)
            ) {
                subscriptionIntent()
            }
        }
    }

    override fun onOpened() {
        binding.viewSwitcherChats.showNext()
    }

    override fun onQuery(text: String) {
        if (text.isNotEmpty())
            homeViewModel.setTextSearch(text)
        else if (text.isEmpty() && homeViewModel.getTextSearch().count() == 1) {
            homeViewModel.setTextSearch("")
        }
        if (text.length >= 2) {
            binding.recyclerViewChats.isVisible = false
            binding.recyclerViewContacts.isVisible = true
            homeViewModel.searchContact(text.toLowerCase(Locale.getDefault()))
        } else {
            refreshView()
        }
    }

    override fun onClosed() {
        refreshView()
        binding.viewSwitcherChats.showPrevious()
    }

    override fun onClosedCompleted() {

    }

    private fun refreshView() {
        binding.recyclerViewChats.isVisible = true
        binding.recyclerViewContacts.isVisible = false
    }
}
