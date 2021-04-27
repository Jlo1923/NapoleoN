package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO

interface GetMessagesSocketListener {

    fun emitSocketClientConversation(listMessagesReceived: MessagesReqDTO)

}