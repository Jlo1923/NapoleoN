package com.naposystems.napoleonchat.ui.blockedContacts

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
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.BlockedContactsFragmentBinding
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.blockedContacts.adapter.BlockedContactsAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

class BlockedContactsFragment : Fragment(), SearchView.OnSearchView {

    companion object {
        private const val EMPTY_STATE = 0
        private const val RECYCLER_VIEW = 1
        private const val SEARCH_NO_RESULT = 2
        fun newInstance() = BlockedContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BlockedContactsViewModel by viewModels { viewModelFactory }
    private val shareContactViewModel: ShareContactViewModel by activityViewModels { viewModelFactory }
    private val contactRepositoryShareViewModel: ContactRepositoryShareViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var binding: BlockedContactsFragmentBinding
    private lateinit var adapter: BlockedContactsAdapter
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
            searchView.setStyleable(Constants.LocationSearchView.OTHER.location)
            searchView.setHint(R.string.text_search)
            searchView.setMenuItem(menu.findItem(R.id.search_blocked_contacts))
            searchView.setListener(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contactRepositoryShareViewModel.getContacts(
            Constants.FriendShipState.BLOCKED.state,
            Constants.LocationGetContact.BLOCKED.location
        )

        viewModel.getBlockedContacts()

        observeBlockedContacts()

        viewModel.webServiceErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar {}
        })

        observeListBlockedContacts()
    }

    override fun onPause() {
        if (::popup.isInitialized) {
            popup.dismiss()
        }
        super.onPause()
    }

    private fun observeListBlockedContacts() {
        viewModel.listBlockedContacts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            binding.viewFlipper.displayedChild =
                if (it.isNotEmpty()) RECYCLER_VIEW else SEARCH_NO_RESULT
        })
    }

    private fun observeBlockedContacts() {
        viewModel.blockedContacts.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            binding.viewFlipper.displayedChild = if (it.isNotEmpty()) RECYCLER_VIEW else EMPTY_STATE
        })
    }

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {
        //Nothing
    }

    override fun onQuery(text: String) {
        when (text.length) {
//            in 1..3 -> showNoResults()
            0 -> showRecycler()
            else -> viewModel.searchLocalBlockedContact(text.toLowerCase(Locale.getDefault()))
        }
    }

    override fun onClosed() {
        showRecycler()
    }

    override fun onClosedCompleted() {}
    //endregion

    /*private fun showNoResults() {
        adapter.submitList(viewModel.blockedContacts.value)
        binding.viewFlipper.displayedChild = SEARCH_NO_RESULT
    }*/

    private fun showRecycler() {
        val blockedContacts = viewModel.blockedContacts.value?.size ?: 0

        if (blockedContacts > 0) {
            binding.viewFlipper.displayedChild = RECYCLER_VIEW
            adapter.submitList(viewModel.blockedContacts.value)
        } else binding.viewFlipper.displayedChild = EMPTY_STATE
    }

    private fun setAdapter() {
        adapter =
            BlockedContactsAdapter(object : BlockedContactsAdapter.BlockedContactsClickListener {
                override fun onClick(item: Contact) {}

                override fun onMoreClick(item: Contact, view: View) {
                    showPopupMenu(view, item)
                }
            })

        binding.recyclerViewBlockedContacts.adapter = adapter
        binding.recyclerViewBlockedContacts.itemAnimator = ItemAnimator()
    }

    private fun showPopupMenu(view: View, item: Contact) {
        popup = PopupMenu(requireContext(), view)
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

    private fun unblockContact(contact: Contact) {
        Utils.generalDialog(
            getString(R.string.text_unblock_contact),
            getString(R.string.text_wish_unblock_contact),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.unblockContact(contact.id)
            showToast(requireContext(), getString(R.string.text_unblocked_contact))
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
