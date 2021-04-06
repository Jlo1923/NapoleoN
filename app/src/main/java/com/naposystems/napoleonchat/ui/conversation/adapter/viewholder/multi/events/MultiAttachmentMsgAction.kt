package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

sealed class MultiAttachmentMsgAction {

    class ShowQuantity(
        val data: Pair<Int, Int>
    ) : MultiAttachmentMsgAction()

    object HideQuantity : MultiAttachmentMsgAction()

}