package com.naposystems.pepito.ui.addContact

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AddContactFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.addContact.FriendShipRequest
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.addContact.adapter.AddContactAdapter
import com.naposystems.pepito.ui.addContact.adapter.FriendshipRequestAdapter
import com.naposystems.pepito.ui.custom.SearchView
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
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
                if (binding.viewSwitcherFriendshipRequest.currentView.id == binding.containerEmptyStateFriendship.id) {
                    binding.viewSwitcherFriendshipRequest.showNext()
                }
            } else {
                if (binding.viewSwitcherFriendshipRequest.currentView.id == binding.recyclerViewFriendshipRequest.id) {
                    binding.viewSwitcherFriendshipRequest.showNext()
                }
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
                if (binding.viewSwitcherSearch.currentView.id == binding.containerEmptyStateSearch.id) {
                    binding.viewSwitcherSearch.showNext()
                }
            } else {
                if (binding.viewSwitcherSearch.currentView.id == binding.recyclerViewContacts.id) {
                    binding.viewSwitcherSearch.showNext()
                }
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
    }

    private fun setupSearchContactAdapter() {
        adapter = AddContactAdapter(object : AddContactAdapter.ClickListener {
            override fun onAddClick(contact: Contact) {
                viewModel.sendFriendshipRequest(contact)
            }
        })
        binding.recyclerViewContacts.adapter = adapter
    }

    //region Implementation SearchView.OnSearchView
    override fun onOpened() {
        if (binding.viewSwitcher.currentView.id == binding.swipeRefresh.id) {
            binding.viewSwitcher.showNext()
        }
    }

    override fun onQuery(text: String) {
        if (text.length >= 4) {
            viewModel.searchContact(text.toLowerCase(Locale.getDefault()))
        } else {
            viewModel.resetContacts()
        }
    }

    override fun onClosed() {
        viewModel.resetContacts()
        viewModel.getFriendshipRequests()
        if (binding.viewSwitcher.currentView.id == binding.viewSwitcherSearch.id) {
            binding.viewSwitcher.showNext()
        }
    }
    //endregion
}