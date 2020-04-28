package com.naposystems.pepito.ui.blockedContacts

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.BlockedContactsFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.blockedContacts.adapter.BlockedContactsAdapter
import com.naposystems.pepito.ui.custom.SearchView
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.ItemAnimator
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BlockedContactsFragment : Fragment(), SearchView.OnSearchView {

    companion object {
        fun newInstance() = BlockedContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BlockedContactsViewModel by viewModels { viewModelFactory }
    private lateinit var shareContactViewModel: ShareContactViewModel
    private lateinit var binding: BlockedContactsFragmentBinding
    private lateinit var adapter: BlockedContactsAdapter
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
            inflater, R.layout.blocked_contacts_fragment, container, false
        )

        setHasOptionsMenu(true)

        binding.lifecycleOwner = this

        setAdapter()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_blocked_contacts, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setHint(R.string.text_search)
            searchView.setMenuItem(menu.findItem(R.id.search_blocked_contacts))
            searchView.setListener(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            shareContactViewModel = ViewModelProvider(activity!!, viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        setupEmptyState()

        viewModel.getBlockedContacts()

        observeBlockedContacts()

        viewModel.webServiceErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar()
        })

        observeListBlockedContacts()
    }

    private fun observeListBlockedContacts() {
        viewModel.listBlockedContacts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcherRecycler.currentView.id == binding.containerSearchNotFound.id) {
                    binding.viewSwitcherRecycler.showNext()
                }
            } else {
                if (binding.viewSwitcherRecycler.currentView.id == binding.containerRecyclerViewBlockedContacts.id) {
                    binding.viewSwitcherRecycler.showNext()
                }
            }
        })
    }

    private fun observeBlockedContacts() {
        viewModel.blockedContacts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcher.currentView.id == binding.emptyState.id) {
                    binding.viewSwitcher.showNext()
                }
            } else {
                if (binding.viewSwitcher.currentView.id == binding.viewSwitcherRecycler.id) {
                    binding.viewSwitcher.showNext()
                }
            }
        })
    }

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {
        //Nothing
    }

    override fun onQuery(text: String) {
        if (text.length >= 4) {
            viewModel.searchLocalBlockedContact(text.toLowerCase(Locale.getDefault()))
        } else {
            refreshView()
        }
    }

    override fun onClosed() {
        refreshView()
    }
    //endregion

    private fun refreshView() {
        adapter.submitList(viewModel.blockedContacts.value)
        if (binding.viewSwitcherRecycler.currentView.id == binding.containerSearchNotFound.id) {
            binding.viewSwitcherRecycler.showNext()
        }
    }

    private fun setAdapter() {
        adapter =
            BlockedContactsAdapter(object : BlockedContactsAdapter.BlockedContactsClickListener {
                override fun onClick(item: Contact) {
                    seeProfile(item)
                }

                override fun onMoreClick(item: Contact, view: View) {
                    showPopupMenu(view, item)
                }
            })

        binding.recyclerViewBlockedContacts.adapter = adapter
        binding.recyclerViewBlockedContacts.itemAnimator = ItemAnimator()
    }

    private fun showPopupMenu(view: View, item: Contact) {
        val popup = PopupMenu(context!!, view)
        popup.menuInflater.inflate(R.menu.menu_block_contact, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.see_profile -> {
                    seeProfile(item)
                }
                R.id.unblock -> {
                    unblockContact(item)
                }
            }
            true
        }
        popup.show()
    }

    private fun seeProfile(contact: Contact) {
        findNavController().navigate(
            BlockedContactsFragmentDirections
                .actionBlockedContactsFragmentToContactProfileFragment(contact.id)
        )
    }

    private fun setupEmptyState() {
        binding.emptyState.setImageEmptyState(R.drawable.image_empty_state_blocked_contacts)
        binding.emptyState.setTitleEmptyState(R.string.text_empty_state_blocked_contacts_title)
        binding.emptyState.setDescriptionEmptyState(R.string.text_empty_state_blocked_contacts_description)
    }

    private fun unblockContact(contact: Contact) {
        Utils.generalDialog(
            getString(R.string.text_unblock_contact),
            getString(
                R.string.text_wish_unblock_contact,
                if (contact.displayNameFake.isEmpty()) {
                    contact.displayName
                } else {
                    contact.displayNameFake
                }
            ),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.unblockContact(contact.id)
            showToast(context!!, getString(R.string.text_unblocked_contact))
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
