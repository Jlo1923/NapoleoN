package com.naposystems.pepito.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.HomeFragmentBinding
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.home.adapter.ConversationAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: HomeViewModel
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

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.home_fragment, container, false)

        adapter = ConversationAdapter(object : ConversationAdapter.ClickListener {
            override fun onClick(item: ConversationAndContact) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToConversationFragment(item.contact)
                )
            }
        })

        binding.recyclerViewChats.adapter = adapter

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
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(HomeViewModel::class.java)

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
}
