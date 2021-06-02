package com.naposystems.napoleonchat.repository.conversationCall

import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface ConversationCallRepository {
    fun getUserDisplayFormat(): Int
    suspend fun getContactById(contactId: Int): ContactEntity?
}