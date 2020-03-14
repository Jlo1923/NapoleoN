package com.naposystems.pepito.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ContactsFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.adapter.ContactsAdapter
import com.naposystems.pepito.ui.custom.SearchView
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class ContactsFragment : Fragment(), SearchView.OnSearchView {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ContactsViewModel
    private lateinit var shareContactViewModel: ShareContactViewModel
    private lateinit var binding: ContactsFragmentBinding
    private lateinit var adapter: ContactsAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var searchView: SearchView

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

        setAdapter()

        binding.swipeRefresh.setOnRefreshListener {
            if (binding.viewSwitcher.nextView.id == binding.lottieEmptyState.id) {
                binding.viewSwitcher.showNext()
            }
            viewModel.getContacts()
            binding.swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ContactsViewModel::class.java)

        try {
            shareContactViewModel = ViewModelProvider(activity!!, viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        binding.viewModel = viewModel

        viewModel.getContacts()

        viewModel.contacts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcher.nextView.id == binding.swipeRefresh.id) {
                    binding.viewSwitcher.showNext()
                }
            } else {
                if (binding.viewSwitcher.nextView.id == binding.lottieEmptyState.id) {
                    binding.viewSwitcher.showNext()
                }
            }
        })

        viewModel.webServiceErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar()
        })

        viewModel.contactsLoaded.observe(viewLifecycleOwner, Observer {
            if (it == true && binding.viewSwitcher.nextView.id == binding.swipeRefresh.id) {
                binding.viewSwitcher.showNext()
            }
        })

        viewModel.contactsForSearch.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcherSearchContact.currentView.id == binding.containerSearchContactNotFound.id) {
                    binding.viewSwitcherSearchContact.showNext()
                }
            } else {
                if (binding.viewSwitcherSearchContact.currentView.id == binding.recyclerViewContacts.id) {
                    binding.viewSwitcherSearchContact.showNext()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contacts, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setHint(R.string.text_search)
            searchView.setMenuItem(menu.findItem(R.id.search_contacts))
            searchView.setListener(this)
        }
    }

    private fun setAdapter() {
        adapter = ContactsAdapter(object : ContactsAdapter.ContactClickListener {
            override fun onClick(item: Contact) {
                goToConversation(item)
            }

            override fun onMoreClick(item: Contact, view: View) {
                val popup = PopupMenu(context!!, view)
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
        })
        binding.recyclerViewContacts.adapter = adapter
    }

    private fun goToConversation(item: Contact) {
        findNavController().navigate(
            ContactsFragmentDirections.actionContactsFragmentToConversationFragment(item)
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
            getString(
                R.string.text_wish_block_contact,
                if (contact.displayNameFake.isEmpty()) {
                    contact.displayName
                } else {
                    contact.displayNameFake
                }
            ),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(contact)
        }
    }

    private fun deleteContact(contact: Contact) {
        generalDialog(
            getString(R.string.text_delete_contact),
            getString(
                R.string.text_wish_delete_contact,
                if (contact.displayNameFake.isEmpty()) {
                    contact.displayName
                } else {
                    contact.displayNameFake
                }
            ),
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
    //endregion

    private fun refreshView() {
        adapter.submitList(viewModel.contacts.value)
        if (binding.viewSwitcherSearchContact.currentView.id == binding.containerSearchContactNotFound.id) {
            binding.viewSwitcherSearchContact.showNext()
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
