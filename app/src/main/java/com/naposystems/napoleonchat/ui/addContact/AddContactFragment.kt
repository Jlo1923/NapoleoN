package com.naposystems.napoleonchat.ui.addContact

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AddContactFragmentBinding
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter
import com.naposystems.napoleonchat.ui.addContact.adapter.FriendshipRequestAdapter
import com.naposystems.napoleonchat.ui.custom.SearchView
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.SnackbarUtils
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
    private lateinit var binding: AddContactFragmentBinding
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
    ): View? {

        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.add_contact_fragment, container, false
        )

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

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_contact, menu)
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            searchView = mainActivity.findViewById(R.id.searchView)
            searchView.setHint(R.string.search_by_nickname)
            searchView.setMenuItem(menu.findItem(R.id.search))
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

    private fun observeFriendshipRequestAcceptedSuccessfully() {
        viewModel.friendshipRequestAcceptedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.getFriendshipRequests()
            }
        })
    }

    private fun observeFriendshipRequestWsError() {
        viewModel.friendshipRequestWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {

                val list = ArrayList<String>()
                list.add(it)

                val snackbarUtils = SnackbarUtils(binding.coordinator, list)

                snackbarUtils.showSnackbar()
            }
        })
    }

    private fun observeFriendshipRequestPutSuccessfully() {
        viewModel.friendshipRequestPutSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.getFriendshipRequests()
            }
        })
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
        viewModel.friendShipRequestSendSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val index = viewModel.users.value!!.indexOf(viewModel.lastFriendshipRequest)
                adapter.updateContact(index)
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
    }

    private fun setupFriendshipRequestsAdapter() {
        friendshipRequestsAdapter =
            FriendshipRequestAdapter(object : FriendshipRequestAdapter.ClickListener {
                override fun onRefuse(friendshipRequest: FriendShipRequest) {
                    viewModel.refuseFriendshipRequest(friendshipRequest)
                }

                override fun onAccept(friendshipRequest: FriendShipRequest) {
                    viewModel.acceptFriendshipRequest(friendshipRequest)
                }

                override fun onCancel(friendshipRequest: FriendShipRequest) {
                    viewModel.cancelFriendshipRequest(friendshipRequest)
                }
            })

        binding.recyclerViewFriendshipRequest.adapter = friendshipRequestsAdapter
        binding.recyclerViewFriendshipRequest.itemAnimator = ItemAnimator()

    }

    private fun setupSearchContactAdapter() {
        adapter = AddContactAdapter(object : AddContactAdapter.ClickListener {
            override fun onAddClick(contact: Contact) {
                viewModel.sendFriendshipRequest(contact)
            }
        })
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