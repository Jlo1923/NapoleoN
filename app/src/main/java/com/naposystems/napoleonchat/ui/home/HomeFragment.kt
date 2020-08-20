package com.naposystems.napoleonchat.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.HomeFragmentBinding
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.home.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_VERSION_KEY
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.generalDialog
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.naposystems.napoleonchat.utility.adapters.verifyPermission
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.napoleonchat.utility.showCaseManager.ShowCaseManager
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import javax.inject.Inject

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: HomeViewModel by viewModels { viewModelFactory }
    private val shareContactViewModel: ShareContactViewModel by viewModels { viewModelFactory }
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
    lateinit var adapter: ConversationAdapter
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }
    private lateinit var textViewBadge: TextView

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var mFirebaseStorage: FirebaseStorage
    private var isShowingVersionDialog: Boolean = false

    private var addContactsMenuItem: MenuItem? = null
    private var homeMenuItem: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        viewModel.verifyMessagesToDelete()

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.home_fragment, container, false
        )

        this.verifyPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            drawableIconId = R.drawable.ic_camera_primary,
            message = R.string.text_explanation_camera_to_receive_calls
        ) {
            //Intentionally empty
        }

        setAdapter()

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

        val disposableNewMessageReceived =
            RxBus.listen(RxEvent.NewFriendshipRequest::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.getFriendshipQuantity()
                }

        disposable.add(disposableNewMessageReceived)

        binding.textViewStatus.isSelected = true

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contactRepositoryShareViewModel.getContacts(
            Constants.FriendShipState.ACTIVE.state
        )

        viewModel.getConversation()

        viewModel.getUserLiveData()

        viewModel.getMessages()

        viewModel.getDeletedMessages()

        viewModel.getFriendshipQuantity()

        viewModel.subscribeToGeneralSocketChannel()

        userDisplayFormatShareViewModel.getUserDisplayFormat()

        timeFormatShareViewModel.getTimeFormat()

        viewModel.conversations?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.submitList(it)
                if (it.isEmpty() && binding.viewSwitcherChats.nextView.id == binding.emptyState.id) {
                    binding.viewSwitcherChats.showNext()
                } else if (it.isNotEmpty() && binding.viewSwitcherChats.nextView.id == binding.recyclerViewChats.id) {
                    binding.viewSwitcherChats.showNext()
                }
                viewModel.resetConversations()
            }
        })

        viewModel.quantityFriendshipRequest.observe(viewLifecycleOwner, Observer {
            if (it != -1) {
                setupBadge(it)
            }
        })

        viewModel.insertSubscription()

        validateSubscriptionTime()

        viewModel.user.observe(viewLifecycleOwner, Observer {
            binding.textViewStatus.text = it.status
        })

        viewModel.jsonNotification.observe(viewLifecycleOwner, Observer { json ->
            if (!json.isNullOrEmpty()) {
                val jsonNotification = JSONObject(json)
                when (jsonNotification.getInt(Constants.NotificationKeys.TYPE_NOTIFICATION)) {
                    Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                        viewModel.getContact(jsonNotification.getInt(Constants.NotificationKeys.CONTACT))
                    }
                    Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                        viewModel.cleanJsonNotification()
                        goToAddContactFragment()
                    }
                    Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                        viewModel.cleanJsonNotification()
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToContactsFragment()
                        )
                    }
                }
            }
        })

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            it?.let { contact ->
                viewModel.cleanJsonNotification()
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(contact)
                )
            }
        })

        (activity as MainActivity).getUser()
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

        val drawerMenu = (activity as MainActivity).getNavView().menu

        val securitySettingMenuItem =
            drawerMenu.findItem(R.id.security_settings).actionView as ConstraintLayout

        ShowCaseManager().apply {
            setListener(object : ShowCaseManager.Listener {
                override fun openSecuritySettings() {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToSecuritySettingsFragment(
                            showShowCase = true
                        )
                    )
                }
            })

            setActivity(requireActivity())
            setFirstView(addContactsMenuItem?.actionView!!)
            setSecondView(binding.fabContacts)
            setThirdView(binding.viewShowCaseStatus)
            setFourthView(homeMenuItem!!)
            setFifthView(securitySettingMenuItem.getChildAt(0))

            showFromFirst()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getJsonNotification()
        /*if (!isShowingVersionDialog && !BuildConfig.DEBUG)
            getRemoteConfig()*/
    }

    private fun getRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getVersion()
                } else {
                    this.showToast("No se han podido obtener el remote config|!!")
                }
            }
    }

    private fun getVersion() {
        val versionApp = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_VERSION_KEY)

        if (versionApp != BuildConfig.VERSION_NAME) {
            Utils.alertDialogInformative(
                title = getString(R.string.text_alert_failure),
                message = getString(R.string.text_update_message, versionApp),
                titleButton = R.string.text_update,
                childFragmentManager = requireContext(),
                clickTopButton = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(
                            "https://play.google.com/store/apps/details?id=com.naposystems.pepito"
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
    }

    private fun validateSubscriptionTime() {
        val freeTrial = viewModel.getFreeTrial()
        val subscriptionTime = viewModel.getSubscriptionTime()

        if (System.currentTimeMillis() > freeTrial) {
            if (System.currentTimeMillis() > subscriptionTime && subscriptionTime == 0L) {
                binding.textViewMessageSubscription.text =
                    getString(R.string.text_free_trial_expired)
                binding.containerSubscription.isVisible = true
            } else {
                if (subscriptionTime > System.currentTimeMillis()) {
                    binding.containerSubscription.isVisible = false
                } else {
                    binding.textViewMessageSubscription.text =
                        getString(R.string.text_expired_subscription)
                    binding.containerSubscription.isVisible = true
                }
            }
        } else {
            binding.containerSubscription.isVisible = false
        }
    }

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
        viewModel.user.value?.let { user ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToStatusFragment(user)
            )
        }
    }

    private fun setAdapter() {
        adapter = ConversationAdapter(
            object : ConversationAdapter.ClickListener {
                override fun onClick(item: MessageAndAttachment) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToConversationFragment(item.contact)
                    )
                }

                override fun onClickAvatar(item: MessageAndAttachment) {
                    seeProfile(item.contact)
                }

                override fun onLongClick(item: MessageAndAttachment, view: View) {
                    val popup = PopupMenu(context!!, view)
                    popup.menuInflater.inflate(R.menu.menu_inbox_conversation, popup.menu)

                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.start_chat_from_inbox ->
                                startConversation(item.contact)
                            R.id.see_profile_from_inbox ->
                                seeProfile(item.contact)
                            R.id.delete_chat_from_inbox ->
                                deleteChat(item.contact)
                            R.id.block_contact_from_inbox ->
                                blockContact(item.contact)
                        }
                        true
                    }
                    popup.show()
                }
            },
            userDisplayFormatShareViewModel.getValUserDisplayFormat(),
            timeFormatShareViewModel.getValTimeFormat()
        )
        binding.recyclerViewChats.adapter = adapter
        binding.recyclerViewChats.itemAnimator = ItemAnimator()

    }

    private fun startConversation(contact: Contact) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToConversationFragment(contact)
        )
    }

    private fun seeProfile(contact: Contact) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToContactProfileFragment(contact.id)
        )
    }

    private fun deleteChat(contact: Contact) {
        generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.deleteConversation(contact.id)
        }
    }

    private fun blockContact(contact: Contact) {
        generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(contact)
        }
    }
}
