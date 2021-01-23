package com.naposystems.napoleonchat.db.dao.messageNotSent

import com.naposystems.napoleonchat.entity.MessageNotSent
import javax.inject.Inject

class MessageNotSentLocalDataSource @Inject constructor(private val dao: MessageNotSentDao) :
    MessageNotSentDataSource {

    override fun insertMessageNotSent(messageNotSent: MessageNotSent) {
        dao.insertMessageNotSent(messageNotSent)
    }

    override fun deleteMessageNotSentByContact(contactId: Int) {
        dao.deleteMessageNotSentByContact(contactId)
    }

    override fun getMessageNotSetByContact(contactId: Int): MessageNotSent {
        return dao.getMessageNotSentByContact(contactId)
    }
}