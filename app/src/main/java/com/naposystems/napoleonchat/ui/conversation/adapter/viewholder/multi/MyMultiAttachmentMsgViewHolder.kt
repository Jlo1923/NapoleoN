package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageMultiBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.ui.conversation.adapter.bindMessageDateSend
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction.OpenMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.*
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.MyMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.ERROR
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.SENDING
import com.naposystems.napoleonchat.utility.extensions.*
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

class MyMultiAttachmentMsgViewHolder(
    private val binding: ConversationItemMyMessageMultiBinding,
    private val viewModel: MyMultiAttachmentMsgViewModel,
    private val listener: MultiAttachmentMsgListener
) : ConversationViewHolder(binding.root, binding.root.context),
    MultiAttachmentMsgItemListener {

    private lateinit var msgAndAttachment: MessageAttachmentRelation
    lateinit var currentAttachments: List<AttachmentEntity>

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
//        super.progressBar = binding.progressBar
//        super.progressBarIndeterminate = binding.progressBarIndeterminate
//        super.imageButtonState = binding.imageButtonState
//        super.textViewCountDown = binding.textViewCountDown
//        super.quote = binding.quote
    }

    companion object {
        fun from(
            parent: ViewGroup,
            viewModel: MyMultiAttachmentMsgViewModel,
            listener: MultiAttachmentMsgListener
        ): MyMultiAttachmentMsgViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMyMessageMultiBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return MyMultiAttachmentMsgViewHolder(binding, viewModel, listener)
        }
    }

    override fun bind(
        item: MessageAttachmentRelation,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        msgAndAttachment = item
        configListenersViews()
        bindViewModel()
        paintAttachments()
        paintUploadFiles()
        paintMoreData(timeFormat)
        paintMessageStatus()
        defineListeners()
    }

    private fun paintMessageStatus() = binding.apply {
        when (msgAndAttachment.messageEntity.status) {
            ERROR.status -> paintMessageError()
            SENDING.status -> paintMessageSending()
            else -> paintMessageOk()
        }
    }

    private fun paintMessageOk() = binding.apply {
        hideViews(progressBarIndeterminate, imageButtonState)
        textViewMsgDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        tryUploadAttachments()
    }

    private fun paintMessageSending() =
        binding.apply {
            progressBarIndeterminate.show()
            paintMessageError()
        }

    private fun paintMessageError() = binding.apply {
        showViews(imageButtonState)
        textViewMsgDate.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_message_error,
            0
        )
    }

    private fun defineListeners() {
        binding.apply {
            imageButtonState.setOnClickListener {
                listener.onMultipleAttachmentMsgAction(
                    MultiAttachmentMsgAction.SendMessageToRemote(
                        msgAndAttachment.messageEntity,
                        msgAndAttachment.attachmentEntityList
                    )
                )
            }
        }
    }

    private fun paintMoreData(timeFormat: Int?) {
        binding.apply {
            textViewMsg.text = msgAndAttachment.messageEntity.body
            textViewMsg.show(msgAndAttachment.messageEntity.body.isNotEmpty())
            timeFormat?.let {
                bindMessageDateSend(
                    textViewMsgDate,
                    msgAndAttachment.messageEntity.createdAt,
                    it
                )
            }
        }
    }

    private fun tryUploadAttachments() {
        val attachmentsFilter = msgAndAttachment.attachmentEntityList.filter {
            it.status == Constants.AttachmentStatus.UPLOAD_CANCEL.status ||
                    it.status == Constants.AttachmentStatus.ERROR.status
        }
        if (attachmentsFilter.isNotEmpty()) {
            viewModel.retryUploadAllFiles(
                attachmentsFilter,
                binding.root.context,
                msgAndAttachment.messageEntity
            )
        }
    }

    private fun paintAttachments() {
        msgAndAttachment.attachmentEntityList.apply {
            when (this.size) {
                1 -> showOneItem(this)
                2 -> showTwoItems(this)
                3 -> showThreeElements(this)
                4 -> showFourItems(this)
                5 -> showFiveItems(this)
                else -> showFiveItems(this)
            }
        }
    }

    private fun paintUploadFiles() = msgAndAttachment.attachmentEntityList.apply {
        val countSent = this.filter { it.isSent() || it.isReceived() || it.isReaded() }
        if (countSent.size == this.size) {
            binding.textViewCountFiles.hide()
        } else {
            val data = Pair(countSent.size, this.size)
            showQuantity(data)
        }
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        when (action) {
            is CancelDownload -> Unit
            is RetryDownload -> Unit
            is CancelUpload -> viewModel.cancelUpload(action.attachmentEntity)
            is RetryUpload -> viewModel.retryUpload(action.attachmentEntity)
            is ViewAttachment -> launchActionViewAttachment(action)
        }
    }

    private fun launchActionViewAttachment(action: ViewAttachment) {
        /**
         * En este punto debemos generar los MultipleAttachmentFileItem para la actividad del preview
         * cada uno de estos elementos posee un messageAndAttachment que nos entregará la informacion
         * del mensaje y los attachments
         */
        val listAttachmentFileItems = currentAttachments.map {
            getMultipleAttachmentFileItemFromAttachmentAndMsg(it, msgAndAttachment)
        }
        val actionForListener = OpenMultipleAttachmentPreview(
            listAttachmentFileItems,
            action.index,
            msgAndAttachment.messageEntity.body
        )
        listener.onMultipleAttachmentMsgAction(actionForListener)
    }

    private fun configListenersViews() = binding.apply {
        viewOneFile.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewTwoFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewThreeFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewFourFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewFiveFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
    }

    private fun bindViewModel() {
        //TODO: habilitar el state cuando logremos inyectar instancias del viewmodel por item
        //viewModel.state.observe(binding.root.context as LifecycleOwner, { handleState(it) })
        viewModel.actions().observe(binding.root.context as LifecycleOwner, { handleActions(it) })
    }

    private fun handleActions(action: MultiAttachmentMsgEvent) {
        when (action) {
            MultiAttachmentMsgEvent.HideQuantity -> binding.textViewCountFiles.hide()
            is MultiAttachmentMsgEvent.ShowQuantity -> showQuantity(action.data)
        }
    }

    private fun handleState(state: MultiAttachmentMsgState) = when (state) {
        is MultiAttachmentMsgState.ShowTwoItem -> showTwoItems(state.listElements)
        is MultiAttachmentMsgState.ShowThreeItem -> showThreeElements(state.listElements)
        is MultiAttachmentMsgState.ShowFourItem -> showFourItems(state.listElements)
        is MultiAttachmentMsgState.ShowFiveItem -> showFiveItems(state.listElements)
        is MultiAttachmentMsgState.ShowMoreItem -> showFiveItems(state.listElements)
    }

    private fun showQuantity(data: Pair<Int, Int>) = binding.apply {
        textViewCountFiles.show()
        textViewCountFiles.text = "${data.first} / ${data.second}"
    }

    private fun showOneItem(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFourFiles, viewFiveFiles)
        showViews(viewOneFile)

        viewOneFile.bindAttachments(listElements, msgAndAttachment.isMine())
    }

    private fun showTwoItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewThreeFiles, viewFourFiles, viewFiveFiles, viewOneFile)
        showViews(viewTwoFiles)
        viewTwoFiles.bindAttachments(listElements, msgAndAttachment.isMine())
    }

    private fun showThreeElements(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewFourFiles, viewFiveFiles, viewOneFile)
        showViews(viewThreeFiles)
        viewThreeFiles.bindAttachments(listElements, msgAndAttachment.isMine())
    }

    private fun showFourItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFiveFiles, viewOneFile)
        showViews(viewFourFiles)
        viewFourFiles.bindAttachments(listElements, msgAndAttachment.isMine())
    }

    private fun showFiveItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFourFiles, viewOneFile)
        showViews(viewFiveFiles)
        viewFiveFiles.bindAttachments(listElements, msgAndAttachment.isMine())
    }

}