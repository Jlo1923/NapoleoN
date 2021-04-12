package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

sealed class MultiAttachmentMsgState {

    class ShowTwoItem(
        val listElements: List<AttachmentEntity>
    ) : MultiAttachmentMsgState()

    class ShowThreeItem(
        val listElements: List<AttachmentEntity>
    ) : MultiAttachmentMsgState()

    class ShowFourItem(
        val listElements: List<AttachmentEntity>
    ) : MultiAttachmentMsgState()

    class ShowFiveItem(
        val listElements: List<AttachmentEntity>
    ) : MultiAttachmentMsgState()

    class ShowMoreItem(
        val listElements: List<AttachmentEntity>
    ) : MultiAttachmentMsgState()

}