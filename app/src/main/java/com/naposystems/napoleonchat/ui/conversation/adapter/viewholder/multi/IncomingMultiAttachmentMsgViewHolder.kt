package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.naposystems.napoleonchat.databinding.ConversationItemIncomingMessageMultiBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction.OpenMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.*
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.IncomingMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.extensions.getMultipleAttachmentFileItemFromAttachmentAndMsg
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.showViews
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
    }

    private fun tryDownloadAttachments() {
        val attachmentsFilter = msgAndAttachment.attachmentEntityList.filter {
            it.status != Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status
        }
        viewModel.retryDownloadAllFiles(attachmentsFilter, binding.root.context)
    }

    private fun paintAttachments() {
        msgAndAttachment.apply {
            when (this.attachmentEntityList.size) {
                1 -> showOneItem(attachmentEntityList)
                2 -> showTwoItems(attachmentEntityList)
                3 -> showThreeElements(attachmentEntityList)
                4 -> showFourItems(attachmentEntityList)
                5 -> showFiveItems(attachmentEntityList)
                else -> showFiveItems(attachmentEntityList)
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
        //viewModel.state.observe(binding.root.context as LifecycleOwner, { handleState(it) })

        viewModel.actions().observe(binding.root.context as LifecycleOwner, { handleActions(it) })
    }

    private fun handleActions(action: MultiAttachmentMsgEvent) {
        when (action) {
            MultiAttachmentMsgEvent.HideQuantity -> binding.textViewCountFiles.hide()
            is MultiAttachmentMsgEvent.ShowQuantity -> showQuantity(action.data)
        }
    }

    private fun showQuantity(data: Pair<Int, Int>) = binding.apply {
        textViewCountFiles.text = "${data.first} / ${data.second}"
    }

    private fun handleState(state: MultiAttachmentMsgState) = when (state) {
        is MultiAttachmentMsgState.ShowTwoItem -> showTwoItems(state.listElements)
        is MultiAttachmentMsgState.ShowThreeItem -> showThreeElements(state.listElements)
        is MultiAttachmentMsgState.ShowFourItem -> showFourItems(state.listElements)
        is MultiAttachmentMsgState.ShowFiveItem -> showFiveItems(state.listElements)
        is MultiAttachmentMsgState.ShowMoreItem -> showFiveItems(state.listElements)
    }

    private fun showOneItem(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFourFiles, viewFiveFiles)
        showViews(viewOneFile)
        viewOneFile.bindAttachments(listElements)
    }

    private fun showTwoItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewOneFile, viewThreeFiles, viewFourFiles, viewFiveFiles)
        showViews(viewTwoFiles)
        viewTwoFiles.bindAttachments(listElements)
    }

    private fun showThreeElements(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewOneFile, viewTwoFiles, viewFourFiles, viewFiveFiles)
        showViews(viewThreeFiles)
        viewThreeFiles.bindAttachments(listElements)
    }

    private fun showFourItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewOneFile, viewTwoFiles, viewThreeFiles, viewFiveFiles)
        showViews(viewFourFiles)
        viewFourFiles.bindAttachments(listElements)
    }

    private fun showFiveItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewOneFile, viewTwoFiles, viewThreeFiles, viewFourFiles)
        showViews(viewFiveFiles)
        viewFiveFiles.bindAttachments(listElements)
    }

}