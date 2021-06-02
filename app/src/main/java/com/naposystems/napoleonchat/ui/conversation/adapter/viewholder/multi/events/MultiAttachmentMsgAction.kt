package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

sealed class MultiAttachmentMsgAction {

    class OpenMultipleAttachmentPreview(
        val listElements: List<MultipleAttachmentFileItem>,
        val index: Int,
        val message: String? = null
    ) : MultiAttachmentMsgAction()

    class SendMessageToRemote(
        val messageEntity: MessageEntity,
        val attachments: List<AttachmentEntity?>
    ) : MultiAttachmentMsgAction()

    object ShowNotInternetMessage : MultiAttachmentMsgAction()

}