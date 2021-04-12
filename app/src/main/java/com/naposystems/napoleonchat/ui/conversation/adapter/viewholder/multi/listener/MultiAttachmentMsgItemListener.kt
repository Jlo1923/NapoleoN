package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener

import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgItemAction

interface MultiAttachmentMsgItemListener {

    /**
     * It helps us to detect the actions launched by an item attachment of the message
     *
     * @param action action to react
     */
    fun onMsgItemFileAction(action: MultiAttachmentMsgItemAction)
}