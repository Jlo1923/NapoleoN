package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem

sealed class MultiAttachmentMsgAction {

    class OpenMultipleAttachmentPreview(
        val listElements: List<MultipleAttachmentFileItem>,
        val index: Int
    ) : MultiAttachmentMsgAction()

}