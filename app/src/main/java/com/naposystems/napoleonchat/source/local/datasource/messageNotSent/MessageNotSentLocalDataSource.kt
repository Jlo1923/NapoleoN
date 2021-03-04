package com.naposystems.napoleonchat.source.local.datasource.messageNotSent

import com.naposystems.napoleonchat.source.local.entity.MessageNotSentEntity

interface MessageNotSentLocalDataSource {

    fun insertMessageNotSent(messageNotSentEntity: MessageNotSentEntity)

    fun deleteMessageNotSentByContact(contactId: Int)

    fun getMessageNotSetByContact(contactId: Int): MessageNotSentEntity
}