package com.naposystems.pepito.ui.conversation

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.ui.actionMode.ActionModeMenu
import com.naposystems.pepito.ui.attachment.AttachmentDialogFragment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversationCamera.ShareConversationCameraViewModel
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationFragment : Fragment() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        fun newInstance() = ConversationFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: ConversationViewModel
    private lateinit var shareViewModel: ShareConversationCameraViewModel
    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var adapter: ConversationAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val args: ConversationFragmentArgs by navArgs()
    private var isEditTextFilled: Boolean = false
    private lateinit var actionMode: ActionModeMenu

    private var clipboard: ClipboardManager? = null
    private var clipData: ClipData? = null

    private val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
            R.anim.scale_up
        )
    }
    private val animationScaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
            R.anim.scale_down
        )
    }

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

        binding.lifecycleOwner = this

        binding.contact = args.contact

        setupActionMode()
        setupAdapter()

        Handler().postDelayed({
            binding.textViewUserStatus.isSelected = true
        }, TimeUnit.SECONDS.toMillis(2))

        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            if (!binding.inputPanel.getFloatingActionButton().isShowingMic()) {
                viewModel.saveMessageLocally(
                    binding.inputPanel.getEditTex().text.toString(),
                    "",
                    args.contact,
                    Constants.IsMine.YES.value
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
            handlerGoDown()
        }

        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
                val text = binding.inputPanel.getEditTex().text.toString()

                if (text.isNotEmpty() && !isEditTextFilled) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = true
                    binding.inputPanel.hideImageButtonCamera()
                } else if (text.isEmpty()) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = false
                    binding.inputPanel.showImageButtonCamera()
                }

            }
        })

        binding.inputPanel.getImageButtonAttachment().setOnClickListener {
            val attachmentDialog = AttachmentDialogFragment()
            attachmentDialog.setListener(object :
                AttachmentDialogFragment.OnAttachmentDialogListener {
                override fun galleryPressed() {

                }

                override fun cameraPressed() {
                    findNavController().navigate(
                        ConversationFragmentDirections
                            .actionConversationFragmentToConversationCameraFragment(
                                viewModel.getUser().id,
                                args.contact.id
                            )
                    )
                }

                override fun locationPressed() {

                }

                override fun audioPressed() {

                }

                override fun documentPressed() {

                }
            })
            attachmentDialog.show(parentFragmentManager, "attachmentDialog")
        }

        binding.inputPanel.getImageButtonCamera().setOnClickListener {
            verifyCameraAndMediaPermission()
        }

        clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shareViewModel = ViewModelProviders.of(activity!!, viewModelFactory)
            .get(ShareConversationCameraViewModel::class.java)

        shareViewModel.hasSendClicked.observe(activity!!, Observer {
            if (it == true) {
                val base64 = shareViewModel.getBase64()

                viewModel.saveMessageWithAttachmentLocally(
                    shareViewModel.message.value!!,
                    "",
                    args.contact,
                    Constants.IsMine.YES.value,
                    base64,
                    shareViewModel.getUri()
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ConversationViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getLocalContact(args.contact.id)

        viewModel.setContact(args.contact)

        viewModel.getLocalMessages()

        viewModel.getMessagesSelected(args.contact.id)

        setConversationBackground()

        viewModel.messagesSelected.observe(
            viewLifecycleOwner, Observer { listMessageAndAttachment ->

                val quantityMessagesOtherUser = listMessageAndAttachment.filter {
                    it.message.isMine == 0
                }.toList().count()

                actionMode.hideCopyButton = false

                setupWidgets(0, View.GONE)

                when (listMessageAndAttachment.count()) {

                    Constants.QUANTITY_TO_HIDE_ACTIONMODE -> {
                        actionMode.mode?.finish()
                        setupWidgets(binding.containerStatus.height, View.VISIBLE)
                    }

                    Constants.QUANTITY_MIN_TO_SHOW_ACTIONMODE -> {
                        listMessageAndAttachment.forEach { messageAndAttachment ->
                            if (messageAndAttachment.attachmentList.count() > Constants.QUANTITY_ATTACHMENTS) {
                                actionMode.hideCopyButton = true
                            }
                        }
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                    }

                    else -> {
                        actionMode.hideCopyButton = true
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                    }
                }
                actionMode.mode?.invalidate()
                actionMode.changeTitle(listMessageAndAttachment.count().toString())
            })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })

        viewModel.deleteMessagesForAllWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })

        viewModel.messageMessages.observe(viewLifecycleOwner, Observer { conversationList ->

            adapter.submitList(conversationList)

            if (conversationList.isNotEmpty()) {
                viewModel.sendMessagesRead()
            }

        })

        viewModel.contactProfile.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                actionBarCustomView.contact = it
            }
        })

        viewModel.stringsCopy.observe(viewLifecycleOwner, Observer {
            if (it.count() == 1) {
                copyDataInClipboard(viewModel.parsingListByTextBlock(it))
                viewModel.resetListStringCopy()
                Toast.makeText(context, R.string.text_message_copied, Toast.LENGTH_LONG).show()
                actionMode.mode!!.finish()
            }
        })

        viewModel.responseDeleteLocalMessages.observe(viewLifecycleOwner, Observer {
            if (it) {
                if(actionMode.mode != null) {
                    actionMode.mode!!.finish()
                }
            }
        })
    }

    private fun setupWidgets(sizePaddingTop: Int, visible: Int) {
        binding.containerStatus.visibility = visible
        binding.imageViewBackground.setPadding(0, sizePaddingTop, 0, 0)
        binding.recyclerViewConversation.setPadding(0, sizePaddingTop, 0, 0)
    }

    private fun showSnackbar(listError: List<String>) {
        val snackbarUtils = SnackbarUtils(binding.coordinator, listError)
        snackbarUtils.showSnackbar()
    }

    private fun handlerGoDown() {
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

    override fun onDetach() {
        (activity as MainActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        (activity as MainActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conversation, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        resetConversationBackground()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_see_contact -> {
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToContactProfileFragment(args.contact.id)
                )
            }
        }

        return true
    }

    private fun copyDataInClipboard(text: String) {
        clipData = ClipData.newPlainText("text", text)
        clipboard?.setPrimaryClip(clipData!!)
    }

    private fun setConversationBackground() {
        if (viewModel.getUser().chatBackground.isNotEmpty()) {
            val uri = Uri.parse(viewModel.getUser().chatBackground)
            val inputStream: InputStream = context!!.contentResolver.openInputStream(uri)!!
            val yourDrawable = Drawable.createFromStream(inputStream, uri.toString())
            yourDrawable.alpha = (255 * 0.3).toInt()
            activity!!.window.setBackgroundDrawable(yourDrawable)
        }
    }

    private fun resetConversationBackground() {
        activity!!.window.setBackgroundDrawableResource(R.color.colorBackground)
    }

    private fun inflateCustomActionBar(inflater: LayoutInflater) {
        actionBarCustomView = DataBindingUtil.inflate(
            inflater, R.layout.conversation_action_bar, null, false
        )

        actionBarCustomView.lifecycleOwner = this

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

        actionBarCustomView.containerDescriptionUser.setOnClickListener {
            findNavController().navigate(
                ConversationFragmentDirections
                    .actionConversationFragmentToContactProfileFragment(args.contact.id)
            )
        }
    }

    private fun setupActionMode() {
        actionMode = ActionModeMenu(
            clickCopy = {
                viewModel.copyMessagesSelected(args.contact.id)
            },
            clickDelete = {moreMessagesOtherContact ->
                if(moreMessagesOtherContact){
                    Utils.alertDialogWithoutNeutralButton(
                        R.string.text_delete_messages,
                        false, context!!,
                        R.string.text_delete_message_for_me,
                        R.string.text_cancel,
                        clickTopButton = {clickTopButton ->
                            if (clickTopButton) {
                                viewModel.deleteMessagesSelected(args.contact.id)
                            }
                        })
                } else {
                    Utils.alertDialogWithNeutralButton(
                        R.string.text_delete_messages,
                        false, context!!,
                        R.string.text_delete_message_for_me,
                        R.string.text_cancel,
                        R.string.text_delete_message_for_all,
                        clickTopButton = {clickTopButton ->
                            if (clickTopButton) {
                                viewModel.deleteMessagesSelected(args.contact.id)
                            }
                        },
                        clickDownButton = {clickDowButton ->
                            if (clickDowButton) {
                                viewModel.deleteMessagesForAll(
                                    args.contact.id,
                                    viewModel.messagesSelected.value!!
                                )
                            }
                        })
                }

            }, clickBack = {
                cleanSelectionMessages()
            })
    }

    private fun setupAdapter() {
        adapter = ConversationAdapter(object : ConversationAdapter.ConversationClickListener {
            override fun onClick(item: Message) {
                if (actionMode.mode != null) {
                    updateStateSelectionMessage(item)
                }
            }

            override fun onLongClick(item: Message) {
                if (actionMode.mode == null) {
                    actionMode.startActionMode(view, R.menu.menu_selection_message)
                    updateStateSelectionMessage(item)
                }
            }
        })

        layoutManager = LinearLayoutManager(context!!)
        layoutManager.reverseLayout = true

        binding.recyclerViewConversation.adapter = adapter
        binding.recyclerViewConversation.layoutManager = layoutManager

        binding.recyclerViewConversation.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    layoutManager.findFirstVisibleItemPosition() <= Constants.QUANTITY_TO_SHOW_FAB_CONVERSATION -> {
                        if (binding.textViewNotificationMessage.visibility == View.VISIBLE) {
                            showFabScroll(View.INVISIBLE, animationScaleDown)
                        }
                    }
                    else -> {
                        if (binding.textViewNotificationMessage.visibility != View.VISIBLE) {
                            showFabScroll(View.VISIBLE, animationScaleUp)
                        }
                        binding.fabGoDown.setOnClickListener {
                            handlerGoDown()
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun showFabScroll(visible: Int, animation: Animation) {
        binding.fabGoDown!!.startAnimation(animation)
        binding.textViewNotificationMessage!!.startAnimation(animation)
        binding.fabGoDown.visibility = visible
        binding.textViewNotificationMessage.visibility = visible
    }

    private fun updateStateSelectionMessage(item: Message) {
        viewModel.updateStateSelectionMessage(args.contact.id, item.id, item.isSelected)
    }

    private fun cleanSelectionMessages() {
        viewModel.cleanSelectionMessages(args.contact.id)
    }

    override fun onPause() {
        if (actionMode.mode != null) {
            actionMode.mode!!.finish()
        }
        super.onPause()
    }

    private fun verifyCameraAndMediaPermission() {

        Dexter.withActivity(activity!!)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                                viewModel.getUser().id,
                                args.contact.id
                            )
                        )
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        Utils.showDialogToInformPermission(
                            context!!,
                            childFragmentManager,
                            R.drawable.ic_camera_primary,
                            R.string.explanation_camera_and_storage_permission,
                            { Utils.openSetting(context!!) },
                            {}
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Utils.showDialogToInformPermission(
                        context!!,
                        childFragmentManager,
                        R.drawable.ic_camera_primary,
                        R.string.explanation_camera_and_storage_permission,
                        { token!!.continuePermissionRequest() },
                        { token!!.cancelPermissionRequest() }
                    )
                }
            }).check()
    }
}
