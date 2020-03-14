package com.naposystems.pepito.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

        binding = DataBindingUtil.inflate(layoutInflater,
            R.layout.home_fragment, container, false)

        setAdapter()

        binding.containerStatus.setOnClickListener{
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
        } catch (e: Exception){
            Timber.e(e)
        }

        viewModel.getContactsAndMessages()

        viewModel.getDeletedMessages()

        viewModel.getFriendshipQuantity()

        viewModel.subscribeToGeneralSocketChannel()

        viewModel.quantityFriendshipRequest.observe(viewLifecycleOwner, Observer {
            if (it != -1) {
                setupBadge(it)
            }
        })

        viewModel.conversations.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
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
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToStatusFragment(viewModel.getUser())
        )
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
        })
        binding.recyclerViewChats.adapter = adapter
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
            "Borrar Conversación!!",
            "Desea eliminar la conversación?!!",
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
