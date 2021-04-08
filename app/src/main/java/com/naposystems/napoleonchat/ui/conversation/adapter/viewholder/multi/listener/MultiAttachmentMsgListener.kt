package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener

import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction

interface MultiAttachmentMsgListener {

    /**
     * this method launches the different actions of a message with multiple attachment that
     * requires interaction from the container fragment of the conversation
     *
     * @param action sealed class with the differents actions
     */
    fun onMultipleAttachmentMsgAction(action: MultiAttachmentMsgAction)

}