package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO

interface HandlerNotificationMessageListener {

    fun emitClientConversation(listMessagesReceived: MessagesReqDTO)

}