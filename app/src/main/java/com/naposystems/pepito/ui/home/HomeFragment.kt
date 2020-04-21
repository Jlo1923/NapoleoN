package com.naposystems.pepito.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.HomeFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.home.adapter.ConversationAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.ItemAnimator
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: HomeViewModel
    private lateinit var shareContactViewModel: ShareContactViewModel
    private val userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val contactRepositoryShareViewModel : ContactRepositoryShareViewModel by viewModels{
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.home_fragment, container, false
        )

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

        val disposableNewMessageReceived = RxBus.listen(RxEvent.NewFriendshipRequest::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.getFriendshipQuantity()
            }

        disposable.add(disposableNewMessageReceived)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeViewModel::class.java)

        try {
            shareContactViewModel = ViewModelProvider(this, viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        viewModel.getUserLiveData()

        viewModel.getMessages()

        contactRepositoryShareViewModel.getContacts()

        viewModel.getDeletedMessages()

        viewModel.getFriendshipQuantity()

        viewModel.subscribeToGeneralSocketChannel()

        userDisplayFormatShareViewModel.getUserDisplayFormat()

        viewModel.conversations.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
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

        viewModel.getJsonNotification()

        viewModel.jsonNotification.observe(viewLifecycleOwner, Observer {json ->
            if (!json.isNullOrEmpty()) {
                val jsonNotification = JSONObject(json)
                when(jsonNotification.get(Constants.TYPE_NOTIFICATION)) {
                    Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                        viewModel.getContact(jsonNotification.get(Constants.TYPE_NOTIFICATION_WITH_CONTACT).toString().toInt())
                    }
                    Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                        viewModel.cleanJsonNotification()
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToAddContactFragment()
                        )
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_inbox, menu)

        val menuItem = menu.findItem(R.id.add_contact)

        val actionView = menuItem.actionView
        textViewBadge = actionView.findViewById(R.id.textView_badge)

        actionView.setOnClickListener {
            onOptionsItemSelected(menuItem)
        }
    }

    private fun validateSubscriptionTime() {
        val freeTrial = viewModel.getFreeTrial()
        val subscriptionTime = viewModel.getSubscriptionTime()

        if (System.currentTimeMillis() > freeTrial){
            if (System.currentTimeMillis() > subscriptionTime && subscriptionTime == 0L){
                binding.textViewMessageSubscription.text = getString(R.string.text_free_trial_expired)
                binding.containerSubscription.isVisible = true
            } else {
                if (subscriptionTime > System.currentTimeMillis()){
                    binding.containerSubscription.isVisible = false
                } else {
                    binding.textViewMessageSubscription.text = getString(R.string.text_expired_subscription)
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
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddContactFragment()
                )
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
        viewModel.user.value?.let {user ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToStatusFragment(user)
            )
        }
    }

    private fun setAdapter() {
        adapter = ConversationAdapter(object : ConversationAdapter.ClickListener {
            override fun onClick(item: ConversationAndContact) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(item.contact)
                )
            }

            override fun onClickAvatar(item: ConversationAndContact) {
                seeProfile(item.contact)
            }

            override fun onLongClick(item: ConversationAndContact, view: View) {
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
        }, userDisplayFormatShareViewModel)
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
            getString(R.string.text_wish_block_contact, contact.displayName),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(contact)
        }
    }
}
