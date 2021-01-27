package com.naposystems.napoleonchat.db.dao.messageNotSent

import com.naposystems.napoleonchat.entity.MessageNotSent

interface MessageNotSentDataSource {

    fun insertMessageNotSent(messageNotSent: MessageNotSent)

    fun deleteMessageNotSentByContact(contactId: Int)

    fun getMessageNotSetByContact(contactId: Int): MessageNotSent
}