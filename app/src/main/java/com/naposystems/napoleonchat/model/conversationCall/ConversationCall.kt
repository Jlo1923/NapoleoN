package com.naposystems.napoleonchat.model.conversationCall

data class ConversationCall(
    val channel: String,
    val contactId: Int,
    val isVideoCall: Boolean
)