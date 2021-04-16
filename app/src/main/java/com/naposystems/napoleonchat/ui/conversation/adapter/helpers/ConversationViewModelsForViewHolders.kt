package com.naposystems.napoleonchat.ui.conversation.adapter.helpers

import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.IncomingMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.MyMultiAttachmentMsgViewModel

data class ConversationViewModelsForViewHolders(
    val viewModelMultiAttachment: MyMultiAttachmentMsgViewModel,
    val viewModelIncomingMultiAttachment: IncomingMultiAttachmentMsgViewModel
)