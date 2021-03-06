package com.naposystems.napoleonchat.source.local.datasource.messageNotSent

import com.naposystems.napoleonchat.source.local.entity.MessageNotSentEntity
import com.naposystems.napoleonchat.source.local.dao.MessageNotSentDao
import javax.inject.Inject

class MessageNotSentLocalDataSourceImp @Inject constructor(private val dao: MessageNotSentDao) :
    MessageNotSentLocalDataSource {

    override fun insertMessageNotSent(messageNotSentEntity: MessageNotSentEntity) {
        dao.insertMessageNotSent(messageNotSentEntity)
    }

    override fun deleteMessageNotSentByContact(contactId: Int) {
        dao.deleteMessageNotSentByContact(contactId)
    }

    override fun getMessageNotSetByContact(contactId: Int): MessageNotSentEntity {
        return dao.getMessageNotSentByContact(contactId)
    }
}