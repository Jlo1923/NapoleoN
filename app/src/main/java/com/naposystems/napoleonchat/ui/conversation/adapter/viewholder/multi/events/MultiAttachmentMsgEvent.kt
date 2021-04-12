package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events

sealed class MultiAttachmentMsgEvent {

    class ShowQuantity(
        val data: Pair<Int, Int>
    ) : MultiAttachmentMsgEvent()

    object HideQuantity : MultiAttachmentMsgEvent()

}