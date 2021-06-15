package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemIncomingMessageMultiBinding
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
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.view.model.DownloadAttachmentsIndicatorModel
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.IncomingMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.*
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.*
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

class IncomingMultiAttachmentMsgViewHolder(
    private val binding: ConversationItemIncomingMessageMultiBinding,
    private val viewModel: IncomingMultiAttachmentMsgViewModel,
    private val listener: MultiAttachmentMsgListener
) : ConversationViewHolder(binding.root, binding.root.context),
    MultiAttachmentMsgItemListener {

    private lateinit var msgAndAttachment: MessageAttachmentRelation
    lateinit var currentAttachments: List<AttachmentEntity>

    companion object {
        fun from(
            parent: ViewGroup,
            viewModel: IncomingMultiAttachmentMsgViewModel,
            listener: MultiAttachmentMsgListener
        ): IncomingMultiAttachmentMsgViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemIncomingMessageMultiBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return IncomingMultiAttachmentMsgViewHolder(binding, viewModel, listener)
        }
    }

    init {
        super.parentContainerMessage = binding.containerIncomingMessage
//        super.progressBar = binding.progress`Bar
//        super.progressBarIndeterminate = binding.progressBarIndeterminate
//        super.imageButtonState = binding.imageButtonState
//        super.textViewCountDown = binding.textViewCountDown
//        super.quote = binding.quote
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
        tryDownloadAttachments()
        paintDownloadFiles()
        paintMoreData(timeFormat)
        paintMessageStatus()
        defineListeners()
    }

    private fun defineListeners() = binding.apply {
        imageButtonState.setOnClickListener {
            if (Utils.isInternetAvailable(binding.root.context)) {
                tryDownloadAttachments()
                paintMessageDownload()
            } else {
                listener.onMultipleAttachmentMsgAction(MultiAttachmentMsgAction.ShowNotInternetMessage)
            }
        }
    }

    private fun paintMessageDownload() = binding.apply {
        showViews(progressBarIndeterminate, imageButtonState)
    }

    private fun paintDownloadFiles() = msgAndAttachment.apply {
        if (this.messageEntity.isReceived()) {
            val countSent =
                this.attachmentEntityList.filter { it.isDownloadComplete() || it.isReceived() || it.isReaded() }
            if (countSent.size == this.attachmentEntityList.size) {
                binding.viewDownloadAttachmentsIndicator.hide()
            } else {
                val data = Pair(countSent.size, this.attachmentEntityList.size)
                showQuantity(data)
            }
        } else {
            val countSent =
                this.attachmentEntityList.filter { it.isDownloadComplete() || it.isReceived() || it.isReaded() }
            if (countSent.size == this.messageEntity.numberAttachments) {
                binding.viewDownloadAttachmentsIndicator.hide()
            } else {
                val data = Pair(countSent.size, this.messageEntity.numberAttachments)
                showQuantity(data)
            }
        }
    }

    private fun paintMoreData(timeFormat: Int?) = binding.apply {
        msgAndAttachment.messageEntity.let {
            textViewMsg.text = it.body
            textViewMsg.show(it.body.isNotEmpty())
            timeFormat?.let { formatTime ->
                bindMessageDateSend(textViewMsgDate, it.createdAt, formatTime)
            }
        }
    }

    private fun tryDownloadAttachments() {
        val attachmentsFilter = msgAndAttachment.attachmentEntityList.filter {
            it.status != Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status &&
                    it.status != Constants.AttachmentStatus.RECEIVED.status &&
                    it.status != Constants.AttachmentStatus.READED.status
        }
        if (attachmentsFilter.isNotEmpty()) {
            viewModel.retryDownloadAllFiles(attachmentsFilter, binding.root.context)
        }
    }

    private fun paintAttachments() {

        val isStateError = msgAndAttachment.messageEntity.status == ERROR.status

        msgAndAttachment.apply {
            when (this.attachmentEntityList.size) {
                1 -> showOneItem(attachmentEntityList, isStateError)
                2 -> showTwoItems(attachmentEntityList, isStateError)
                3 -> showThreeElements(attachmentEntityList, isStateError)
                4 -> showFourItems(attachmentEntityList, isStateError)
                5 -> showFiveItems(attachmentEntityList, isStateError)
                else -> showFiveItems(attachmentEntityList, isStateError)
            }
        }
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        when (action) {
            is CancelDownload -> viewModel.cancelDownload(action.attachmentEntity)
            is RetryDownload -> viewModel.retryDownload(
                action.attachmentEntity,
                binding.root.context
            )
            is ViewAttachment -> launchActionViewAttachment(action)
            is CancelUpload -> Unit
            is RetryUpload -> Unit
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
        viewOneFile.defineListener(this@IncomingMultiAttachmentMsgViewHolder)
        viewTwoFiles.defineListener(this@IncomingMultiAttachmentMsgViewHolder)
        viewThreeFiles.defineListener(this@IncomingMultiAttachmentMsgViewHolder)
        viewFourFiles.defineListener(this@IncomingMultiAttachmentMsgViewHolder)
        viewFiveFiles.defineListener(this@IncomingMultiAttachmentMsgViewHolder)
    }

    private fun bindViewModel() {
        //TODO: habilitar el state cuando logremos inyectar instancias del viewmodel por item
        viewModel.actions().observe(binding.root.context as LifecycleOwner, { handleActions(it) })
    }

    private fun handleActions(action: MultiAttachmentMsgEvent) {
        when (action) {
            MultiAttachmentMsgEvent.HideQuantity -> binding.viewDownloadAttachmentsIndicator.hide()
            is MultiAttachmentMsgEvent.ShowQuantity -> showQuantity(action.data)
        }
    }

    private fun showQuantity(data: Pair<Int, Int>) = binding.apply {
        val data = DownloadAttachmentsIndicatorModel(
            msgAndAttachment.messageEntity.status,
            data.first,
            data.second
        )
        binding.viewDownloadAttachmentsIndicator.apply {
            show()
            bindDataInfo(data)
        }
    }

    private fun showOneItem(listElements: List<AttachmentEntity>, isStateError: Boolean) =
        binding.apply {
            currentAttachments = listElements
            hideViews(viewTwoFiles, viewThreeFiles, viewFourFiles, viewFiveFiles)
            showViews(viewOneFile)
            viewOneFile.bindAttachments(listElements, isStateError)
        }

    private fun showTwoItems(listElements: List<AttachmentEntity>, isStateError: Boolean) =
        binding.apply {
            currentAttachments = listElements
            hideViews(viewOneFile, viewThreeFiles, viewFourFiles, viewFiveFiles)
            showViews(viewTwoFiles)
            viewTwoFiles.bindAttachments(listElements, isStateError)
        }

    private fun showThreeElements(listElements: List<AttachmentEntity>, isStateError: Boolean) =
        binding.apply {
            currentAttachments = listElements
            hideViews(viewOneFile, viewTwoFiles, viewFourFiles, viewFiveFiles)
            showViews(viewThreeFiles)
            viewThreeFiles.bindAttachments(listElements, isStateError)
        }

    private fun showFourItems(listElements: List<AttachmentEntity>, isStateError: Boolean) =
        binding.apply {
            currentAttachments = listElements
            hideViews(viewOneFile, viewTwoFiles, viewThreeFiles, viewFiveFiles)
            showViews(viewFourFiles)
            viewFourFiles.bindAttachments(listElements, isStateError)
        }

    private fun showFiveItems(listElements: List<AttachmentEntity>, isStateError: Boolean) =
        binding.apply {
            currentAttachments = listElements
            hideViews(viewOneFile, viewTwoFiles, viewThreeFiles, viewFourFiles)
            showViews(viewFiveFiles)
            viewFiveFiles.bindAttachments(listElements, isStateError)
        }

    private fun paintMessageStatus() = binding.apply {
        when (msgAndAttachment.messageEntity.status) {
            ERROR.status -> paintMessageError()
            //SENDING.status -> paintMessageSending()
            else -> paintMessageOk()
        }
    }

    private fun paintMessageOk() = binding.apply {
        hideViews(progressBarIndeterminate, imageButtonState)
        removeIconErrorMsg()
        //tryUploadAttachments()
    }

    private fun paintMessageError() = binding.apply {
        showViews(imageButtonState)
        hideViews(progressBarIndeterminate)
        showIconErrorMsg()
    }

    private fun ConversationItemIncomingMessageMultiBinding.showIconErrorMsg() =
        textViewMsgDate.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_message_error,
            0
        )

    private fun ConversationItemIncomingMessageMultiBinding.removeIconErrorMsg() {
        textViewMsgDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }
}