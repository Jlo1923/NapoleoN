package com.naposystems.napoleonchat.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ContactsFragmentBinding
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.contacts.adapter.ContactsAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.custom.emptyState.EmptyStateCustomView
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils.Companion.generalDialog
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ContactsFragment : Fragment(), SearchView.OnSearchView, EmptyStateCustomView.OnEventListener {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ContactsViewModel by viewModels { viewModelFactory }
    private val userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val contactRepositoryShareViewModel: ContactRepositoryShareViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var shareContactViewModel: ShareContactViewModel
    private lateinit var binding: ContactsFragmentBinding
    private lateinit var adapter: ContactsAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var searchView: SearchView
    private lateinit var popup: PopupMenu

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        try {
            shareContactViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        viewModel.resetTextSearch()

        binding.viewModel = viewModel

        getContacts()

        observeContacts()

        viewModel.webServiceErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar()
        })

        viewModel.contactsLoaded.observe(viewLifecycleOwner, Observer {
            if (it == true && binding.viewSwitcher.nextView.id == binding.swipeRefresh.id) {
                binding.viewSwitcher.showNext()
            }
        })

        observeContactsForSearch()
    }

    private fun getContacts() {
        contactRepositoryShareViewModel.getContacts(
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
                        Contact(
                            0,
                            displayName = getString(R.string.text_add_new_contact)
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
        if (::popup.isInitialized){
            popup.dismiss()
        }
        super.onPause()
    }

    private fun setAdapter() {
        adapter = ContactsAdapter(object : ContactsAdapter.ContactClickListener {
            override fun onClick(item: Contact) {
                if (item.id != 0) {
                    goToConversation(item)
                } else {
                    verifyStateSearch()
                }
            }

            override fun onMoreClick(item: Contact, view: View) {
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
        }, userDisplayFormatShareViewModel)
        binding.recyclerViewContacts.adapter = adapter
        binding.recyclerViewContacts.itemAnimator = ItemAnimator()

    }

    private fun goToConversation(item: Contact) {
        searchView.close()
        findNavController().navigate(
            ContactsFragmentDirections.actionContactsFragmentToConversationFragment(item)
        )
    }

    private fun goToAddContacts() {
        findNavController().navigate(
            ContactsFragmentDirections.actionContactsFragmentToAddContactFragment(
                location = Constants.LocationAddContact.CONTACTS.location,
                text = viewModel.getTextSearch()
            )
        )
    }

    private fun seeProfile(contact: Contact) {
        findNavController().navigate(
            ContactsFragmentDirections
                .actionContactsFragmentToContactProfileFragment(contact.id)
        )
    }

    private fun blockedContact(contact: Contact) {
        generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(contact)
        }
    }

    private fun deleteContact(contact: Contact) {
        generalDialog(
            getString(R.string.text_delete_contact),
            getString(R.string.text_wish_delete_contact),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendDeleteContact(contact)
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
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
