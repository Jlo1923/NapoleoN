package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

sealed class MultiAttachmentMsgAction {

    class OpenMultipleAttachmentPreview(
        val listElements: List<AttachmentEntity>,
        val index: Int
    ) : MultiAttachmentMsgAction()

}