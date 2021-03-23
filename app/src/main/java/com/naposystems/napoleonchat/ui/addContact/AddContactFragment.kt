package com.naposystems.napoleonchat.ui.addContact

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AddContactFragmentBinding
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter
import com.naposystems.napoleonchat.ui.addContact.adapter.FriendshipRequestAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddContactFragment : Fragment(), SearchView.OnSearchView {

    companion object {
        fun newInstance() = AddContactFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AddContactViewModel by viewModels { viewModelFactory }
    private val shareViewModel: FriendShipActionShareViewModel by viewModels { viewModelFactory }
    private var _binding: AddContactFragmentBinding? = null
    private val binding get() = _binding!!

    private val args: AddContactFragmentArgs by navArgs()
    private lateinit var mainActivity: MainActivity
    private lateinit var searchView: SearchView
    private lateinit var adapter: AddContactAdapter
    private lateinit var friendshipRequestsAdapter: FriendshipRequestAdapter
    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        _binding = AddContactFragmentBinding.inflate(layoutInflater, container, false)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getFriendshipRequests()
        }

        setupFriendshipRequestsAdapter()
        setupSearchContactAdapter()

        val disposableNewMessageReceived = RxBus.listen(RxEvent.NewFriendshipRequest::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.getFriendshipRequests()
            }

        disposable.add(disposableNewMessageReceived)

        val disposableCancelOrRejectFriendshipRequest =
            RxBus.listen(RxEvent.CancelOrRejectFriendshipRequestEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.getFriendshipRequests()
                }

        disposable.add(disposableCancelOrRejectFriendshipRequest)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_contact, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setMenuItem(menu.findItem(R.id.search))
            searchView.setStyleable(Constants.LocationSearchView.OTHER.location)
            searchView.setHint(R.string.search_by_nickname)
            searchView.setListener(this)
            if (args.location == Constants.LocationAddContact.CONTACTS.location) {
                if (!searchView.isOpened()) {
                    searchView.showSearchView()
                }
                searchView.setTextSearch(args.text)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getFriendshipRequests()

        observeUsers()

        observeFriendShipRequestSend()

        observeFriendshipRequests()

        observeFriendshipRequestPutSuccessfully()

        observeFriendshipRequestWsError()

        observeFriendshipRequestAcceptedSuccessfully()
    }

    override fun onStop() {
        super.onStop()
        searchView.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setListener(null)
        _binding = null
    }

    private fun observeFriendshipRequestAcceptedSuccessfully() {
        shareViewModel.friendshipRequestAcceptedSuccessfully.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.validateIfExistsOffer()
                viewModel.getFriendshipRequests()
            }
        })
    }

    private fun observeFriendshipRequestPutSuccessfully() {
        shareViewModel.friendshipRequestPutSuccessfully.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.validateIfExistsOffer()
                viewModel.getFriendshipRequests()
            }
        })
    }

    private fun observeFriendshipRequestWsError() {
        shareViewModel.friendshipRequestWsError.observe(viewLifecycleOwner, Observer {
            showError(it)
        })

        viewModel.friendshipRequestWsError.observe(viewLifecycleOwner, Observer {
            showError(it)
        })
    }

    private fun showError(error: String) {
        if (error.isNotEmpty()) {
            val list = ArrayList<String>()
            list.add(error)

            val snackbarUtils = SnackbarUtils(binding.coordinator, list)

            snackbarUtils.showSnackbar {}
        }
    }

    private fun observeFriendshipRequests() {
        viewModel.friendshipRequests.observe(viewLifecycleOwner, Observer {
            friendshipRequestsAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
            if (it.isNotEmpty()) {
                if (binding.viewSwitcherFriendshipRequest.currentView.id == binding.emptyStateFriendshipRequest.id) {
                    binding.viewSwitcherFriendshipRequest.showNext()
                }
            } else {
                if (binding.viewSwitcherFriendshipRequest.currentView.id == binding.recyclerViewFriendshipRequest.id) {
                    binding.viewSwitcherFriendshipRequest.showNext()
                }
            }
            if (args.location == Constants.LocationAddContact.HOME.location) {
                validateSearch()
            }
        })
    }

    private fun observeFriendShipRequestSend() {
        viewModel.friendShipRequestSendSuccessfully.observe(viewLifecycleOwner, {
            if (it != null) {
                adapter.updateContact(it)
            }
        })
    }

    private fun observeUsers() {
        viewModel.users.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                if (binding.viewSwitcherSearch.currentView.id == binding.emptyStateSearch.id) {
                    binding.viewSwitcherSearch.showNext()
                }
            } else {
                if (binding.viewSwitcherSearch.currentView.id == binding.recyclerViewContacts.id) {
                    binding.viewSwitcherSearch.showNext()
                }
                binding.emptyStateSearch.imageViewSetVisibility(false)
                binding.emptyStateSearch.setTitleEmptyState(R.string.text_empty_state_search_contacts)
                binding.emptyStateSearch.textViewTitleSetVisibility(true)
            }
        })

        viewModel.updateItem.observe(viewLifecycleOwner, {
            adapter.updateContactRequest(it)
        })

    }

    private fun setupFriendshipRequestsAdapter() {
        friendshipRequestsAdapter =
            FriendshipRequestAdapter(object : FriendshipRequestAdapter.ClickListener {
                override fun onRefuse(friendshipRequest: FriendShipRequest) {
                    shareViewModel.refuseFriendshipRequest(friendshipRequest)
                }

                override fun onAccept(friendshipRequest: FriendShipRequest) {
                    shareViewModel.acceptFriendshipRequest(friendshipRequest)
                }

                override fun onCancel(friendshipRequest: FriendShipRequest) {
                    shareViewModel.cancelFriendshipRequest(friendshipRequest)
                }
            })

        binding.recyclerViewFriendshipRequest.adapter = friendshipRequestsAdapter
        binding.recyclerViewFriendshipRequest.itemAnimator = ItemAnimator()

    }

    private fun setupSearchContactAdapter() {
        adapter = AddContactAdapter(requireContext(), object : AddContactAdapter.ClickListener {
            override fun onAddClick(contact: Contact) {
                viewModel.sendFriendshipRequest(contact)
            }

            override fun onOpenChat(contact: Contact) {
                val c = viewModel.getContact(contact)
                if (c != null)
                    findNavController().navigate(
                        AddContactFragmentDirections.actionAddContactFragmentToConversationFragment(
                            c
                        )
                    )
            }

            override fun onAcceptRequest(contact: Contact, state: Boolean) {

                val request = viewModel.acceptOrRefuseRequest(contact, state)
                if (state) shareViewModel.acceptFriendshipRequest(request)
                else shareViewModel.refuseFriendshipRequest(request)

            }
        })
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContacts.adapter = adapter
        binding.recyclerViewContacts.itemAnimator = ItemAnimator()
    }

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {

        if (binding.viewSwitcher.currentView.id == binding.swipeRefresh.id) {
            binding.viewSwitcher.showNext()
            binding.emptyStateSearch.imageViewSetVisibility(true)
            binding.emptyStateSearch.setTitleEmptyState(R.string.text_emptystate_search_friends_title)
            binding.emptyStateSearch.textViewTitleSetVisibility(true)
        }
    }

    override fun onQuery(text: String) {
        if (text.length >= 3) {
            viewModel.searchContact(text.toLowerCase(Locale.getDefault()))
        } else {
            binding.emptyStateSearch.imageViewSetVisibility(true)
            binding.emptyStateSearch.setTitleEmptyState(R.string.text_emptystate_search_friends_title)
            binding.emptyStateSearch.textViewTitleSetVisibility(true)
        }
    }

    override fun onClosed() {
        viewModel.resetContacts()
        viewModel.getFriendshipRequests()
        if (binding.viewSwitcher.currentView.id == binding.viewSwitcherSearch.id) {
            binding.viewSwitcher.showNext()
        }
        binding.emptyStateSearch.setTitleEmptyState(R.string.text_empty_state_friendship_description)
    }

    override fun onClosedCompleted() {}
    //endregion

    private fun validateSearch() {
        if (!searchView.isOpened() &&
            viewModel.getUsers()?.count()!! <= 0 &&
            viewModel.getRequestSend()?.count()!! <= 0 &&
            viewModel.getSearchOpened() == false
        ) {
            searchView.showSearchView()
            viewModel.setSearchOpened()
        }
    }

}