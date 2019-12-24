package com.naposystems.pepito.ui.conversation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.entity.Conversation
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class ConversationFragment : Fragment() {

    companion object {
        fun newInstance() = ConversationFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: ConversationViewModel
    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val args: ConversationFragmentArgs by navArgs()
    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inflateCustomActionBar(inflater)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_fragment, container, false
        )

        binding.contact = args.contact

        setupAdapter()

        Handler().postDelayed({
            binding.textViewUserStatus.isSelected = true
        }, TimeUnit.SECONDS.toMillis(2))

        val disposableNewMessageReceived = RxBus.listen(RxEvent.NewMessageReceivedEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val jsonObjectData = it.jsonObject.getJSONObject("data")

                val contactId = jsonObjectData.getInt("contact_id")

                viewModel.getRemoteMessages(it.channelName, contactId)
            }

        disposable.add(disposableNewMessageReceived)

        binding.buttonSend.setOnClickListener {

            viewModel.saveConversationLocally(
                binding.textInputEditTextMessage.text.toString(),
                "message",
                args.contact,
                Constants.IsMine.YES.value
            )

            with(binding.textInputEditTextMessage) {
                setText("")
            }
        }

        return binding.root
    }

    private fun inflateCustomActionBar(inflater: LayoutInflater) {
        actionBarCustomView = DataBindingUtil.inflate(
            inflater, R.layout.conversation_action_bar, null, false
        )

        with((activity as MainActivity).supportActionBar!!) {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHasOptionsMenu(true)
            customView = actionBarCustomView.root
        }

        actionBarCustomView.contact = args.contact

        actionBarCustomView.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        adapter = ConversationAdapter(ConversationAdapter.ConversationClickListener {
            Toast.makeText(context!!, it.body, Toast.LENGTH_SHORT).show()
        })

        layoutManager = LinearLayoutManager(context!!)
        layoutManager.reverseLayout = true

        binding.recyclerViewConversation.adapter = adapter
        binding.recyclerViewConversation.layoutManager = layoutManager
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ConversationViewModel::class.java)

        viewModel.subscribeToChannel(args.contact)

        viewModel.getLocalMessages()

        viewModel.getRemoteMessages(viewModel.channelName.value!!, args.contact.id)

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        viewModel.conversationMessages.observe(viewLifecycleOwner, Observer { conversationList ->

            if (conversationList.isNotEmpty()) {

                /*val mutableList: MutableList<Conversation> = ArrayList()

                mutableList.addAll(conversationList.sortedBy { it.id })

                val conversationGrouped = mutableList.groupBy {
                    if (it.createdAt.isNotEmpty()) {
                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val calendar = Calendar.getInstance(TimeZone.getDefault())
                        calendar.timeInMillis = it.createdAt.toLong() * 1000
                        calendar.set(Calendar.HOUR, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.time
                    }
                }

                for (groupedKey in conversationGrouped.entries) {
                    val indexFirst = mutableList.indexOf(groupedKey.value[0])

                    Timber.d(indexFirst.toString())
                }*/

                adapter.submitList(conversationList)

                Handler().postDelayed({
                    if (adapter.itemCount > 0) {
                        val smoothScroller: RecyclerView.SmoothScroller =
                            object : LinearSmoothScroller(context) {
                                override fun getVerticalSnapPreference(): Int {
                                    return SNAP_TO_START
                                }
                            }

                        smoothScroller.targetPosition = 0

                        layoutManager.startSmoothScroll(smoothScroller)
                    }
                }, 300)

            }
        })
    }

    override fun onDetach() {
        (activity as MainActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        (activity as MainActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conversation, menu)
    }

    override fun onDestroy() {
        disposable.clear()
        disposable.dispose()
        viewModel.unSubscribeToChannel(args.contact)
        super.onDestroy()
    }
}
