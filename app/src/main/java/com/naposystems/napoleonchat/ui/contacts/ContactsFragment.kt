package com.naposystems.napoleonchat.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ContactsFragmentBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.contacts.adapter.ContactsAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.custom.emptyState.EmptyStateCustomView
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import java.util.*
import javax.inject.Inject

class ContactsFragment : BaseFragment(), SearchView.OnSearchView,
    EmptyStateCustomView.OnEventListener {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: ContactsViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var handlerDialog: HandlerDialog

    private val userDisplayFormatDialogViewModel: UserDisplayFormatDialogViewModel by activityViewModels {
        viewModelFactory
    }

    private val contactSharedViewModel: ContactSharedViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ContactsFragmentBinding
    private lateinit var adapter: ContactsAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var searchView: SearchView
    private lateinit var popup: PopupMenu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.contacts_fragment, container, false
        )

        setHasOptionsMenu(true)

        binding.lifecycleOwner = this

        binding.emptyState.setListener(this)

        binding.emptyStateSearch.setListener(this)

        setAdapter()

        binding.swipeRefresh.setOnRefreshListener {
            getContacts()
            binding.swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        try {
//            contactSharedViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
//                .get(ContactSharedViewModel::class.java)
//        } catch (e: Exception) {
//            Timber.e(e)
//        }

        viewModel.resetTextSearch()

        binding.viewModel = viewModel

        getContacts()

        observeContacts()

        viewModel.webServiceErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar {}
        })

        viewModel.contactsLoaded.observe(viewLifecycleOwner, Observer {
            if (it == true && binding.viewSwitcher.nextView.id == binding.swipeRefresh.id) {
                binding.viewSwitcher.showNext()
            }
        })

        observeContactsForSearch()
    }

    private fun getContacts() {
        contactSharedViewModel.getContacts(
            Constants.FriendShipState.ACTIVE.state,
            Constants.LocationGetContact.OTHER.location
        )
        viewModel.getLocalContacts()
    }

    private fun observeContactsForSearch() {
        viewModel.contactsForSearch.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcherSearchContact.currentView.id == binding.emptyStateSearch.id) {
                    binding.viewSwitcherSearchContact.showNext()
                }
            } else {
                if (binding.viewSwitcherSearchContact.currentView.id == binding.recyclerViewContacts.id) {
                    binding.viewSwitcherSearchContact.showNext()
                }
            }
        })
    }

    private fun observeContacts() {
        viewModel.contacts.observe(viewLifecycleOwner, Observer { listContacts ->
            if (listContacts != null) {
                if (listContacts.count() >= 1) {
                    listContacts.add(
                        0,
                        ContactEntity(
                            0,
                            displayName = "",
                            displayNameFake = getString(R.string.text_add_new_contact)
                        )
                    )
                    listContacts.sortBy { contact ->
                        contact.getNickName()
                    }
                }
                adapter.submitList(listContacts)
                if (listContacts.isNotEmpty()) {
                    if (binding.viewSwitcher.nextView.id == binding.viewSwitcherSearchContact.id) {
                        binding.viewSwitcher.showNext()
                    }
                    if (binding.viewSwitcherSearchContact.nextView.id == binding.recyclerViewContacts.id) {
                        binding.viewSwitcherSearchContact.showNext()
                    }
                } else {
                    if (binding.viewSwitcher.nextView.id == binding.emptyState.id) {
                        binding.viewSwitcher.showNext()
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contacts, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setStyleable(Constants.LocationSearchView.OTHER.location)
            searchView.setHint(R.string.text_search_contact)
            searchView.setMenuItem(menu.findItem(R.id.search_contacts))
            searchView.setListener(this)
        }
    }

    override fun onPause() {
        if (::popup.isInitialized) {
            popup.dismiss()
        }
        super.onPause()
    }

    private fun setAdapter() {
        adapter = ContactsAdapter(object : ContactsAdapter.ContactClickListener {
            override fun onClick(item: ContactEntity) {
                if (item.id != 0) {
                    goToConversation(item)
                } else {
                    verifyStateSearch()
                }
            }

            override fun onMoreClick(item: ContactEntity, view: View) {
                popup = PopupMenu(context!!, view)
                popup.menuInflater.inflate(R.menu.menu_popup_contact, popup.menu)

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.start_chat ->
                            goToConversation(item)
                        R.id.see_profile ->
                            seeProfile(item)
                        R.id.block_contact ->
                            blockedContact(item)
                        R.id.delete_contact ->
                            deleteContact(item)
                    }
                    true
                }
                popup.show()
            }
        }, userDisplayFormatDialogViewModel)
        binding.recyclerViewContacts.adapter = adapter
        binding.recyclerViewContacts.itemAnimator = ItemAnimator()

    }

    private fun goToConversation(item: ContactEntity) {
        searchView.close()
        findNavController().currentDestination?.getAction(R.id.action_contactsFragment_to_conversationFragment)
            ?.let {
                findNavController().navigate(
                    ContactsFragmentDirections.actionContactsFragmentToConversationFragment(item)
                )
            }
    }

    private fun goToAddContacts() {
        findNavController().navigate(
            ContactsFragmentDirections.actionContactsFragmentToAddContactFragment(
                location = Constants.LocationAddContact.CONTACTS.location,
                text = viewModel.getTextSearch()
            )
        )
    }

    private fun seeProfile(contact: ContactEntity) {
        findNavController().navigate(
            ContactsFragmentDirections
                .actionContactsFragmentToContactProfileFragment(contact.id)
        )
    }

    private fun blockedContact(contact: ContactEntity) {
        handlerDialog.generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            contactSharedViewModel.sendBlockedContact(contact)
        }
    }

    private fun deleteContact(contact: ContactEntity) {
        handlerDialog.generalDialog(
            getString(R.string.text_delete_contact),
            getString(R.string.text_wish_delete_contact),
            true,
            childFragmentManager
        ) {
            contactSharedViewModel.sendDeleteContact(contact)
        }
    }

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {
        binding.swipeRefresh.isEnabled = false
    }

    override fun onQuery(text: String) {
        if (text.isNotEmpty())
            viewModel.setTextSearch(text)
        else if (text.isEmpty() && viewModel.getTextSearch().count() == 1) {
            viewModel.setTextSearch("")
        }
        if (text.length >= 4) {
            viewModel.searchContact(text.toLowerCase(Locale.getDefault()))
        } else {
            refreshView()
        }
    }

    override fun onClosed() {
        refreshView()
        binding.swipeRefresh.isEnabled = true
    }

    override fun onClosedCompleted() {
        goToAddContacts()
    }
    //endregion

    private fun refreshView() {
        adapter.submitList(viewModel.contacts.value)
        if (binding.viewSwitcherSearchContact.currentView.id == binding.emptyStateSearch.id) {
            binding.viewSwitcherSearchContact.showNext()
        }
    }

    override fun onAddContact(click: Boolean) {
        verifyStateSearch()
    }

    private fun verifyStateSearch() {
        if (!searchView.isOpened()) {
            goToAddContacts()
        } else {
            searchView.close(Constants.LocationAddContact.CONTACTS.location)
        }
    }

    override fun onStop() {
        super.onStop()
        searchView.close()
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
