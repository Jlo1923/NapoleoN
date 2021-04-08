package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.naposystems.napoleonchat.databinding.ConversationItemMyMessageMultiBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction.OpenMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction.*
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgItemListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgListener
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

class MyMultiAttachmentMsgViewHolder(
    private val binding: ConversationItemMyMessageMultiBinding,
    private val viewModel: MyMultiAttachmentMsgViewModel,
    private val listener: MultiAttachmentMsgListener
) : ConversationViewHolder(binding.root, binding.root.context),
    MultiAttachmentMsgItemListener {

    private lateinit var msgAndAttachment: MessageAttachmentRelation
    lateinit var currentAttachments: List<AttachmentEntity>

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
        viewModel.getAttachmentsInMessage(item.messageEntity.id)
        configListenersViews()
        bindViewModel()
    }

    override fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction) {
        when (action) {
            is CancelDownload -> viewModel.cancelDownload(action.attachmentEntity)
            is CancelUpload -> viewModel.cancelUpload(action.attachmentEntity)
            is RetryDownload -> viewModel.retryDownload(action.attachmentEntity)
            is RetryUpload -> viewModel.retryUpload(action.attachmentEntity)
            is ViewAttachment -> launchActionViewAttachment(action)
        }
    }

    private fun launchActionViewAttachment(action: ViewAttachment) {
        val actionForListener = OpenMultipleAttachmentPreview(
            currentAttachments,
            action.index
        )
        listener.onMultipleAttachmentMsgAction(actionForListener)
    }

    private fun configListenersViews() = binding.apply {
        viewTwoFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewThreeFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewFourFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
        viewFiveFiles.defineListener(this@MyMultiAttachmentMsgViewHolder)
    }

    private fun bindViewModel() {
        viewModel.state.observe(binding.root.context as LifecycleOwner, { handleState(it) })
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

    private fun showTwoItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewThreeFiles, viewFourFiles, viewFiveFiles)
        showViews(viewTwoFiles)
        viewTwoFiles.bindAttachments(listElements)
    }

    private fun showThreeElements(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewFourFiles, viewFiveFiles)
        showViews(viewThreeFiles)
        viewThreeFiles.bindAttachments(listElements)
    }

    private fun showFourItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFiveFiles)
        showViews(viewFourFiles)
        viewFourFiles.bindAttachments(listElements)
    }

    private fun showFiveItems(listElements: List<AttachmentEntity>) = binding.apply {
        currentAttachments = listElements
        hideViews(viewTwoFiles, viewThreeFiles, viewFourFiles)
        showViews(viewFiveFiles)
        viewFiveFiles.bindAttachments(listElements)
    }

}