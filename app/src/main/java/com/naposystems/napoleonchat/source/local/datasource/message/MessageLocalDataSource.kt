package com.naposystems.napoleonchat.source.local.datasource.message

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface MessageLocalDataSource {

    suspend fun getMessageByWebId(webId: String, decrypt: Boolean): MessageAttachmentRelation?

    suspend fun getMessageById(id: Int, decrypt: Boolean): MessageAttachmentRelation?

    suspend fun getMessageByIdAsLiveData(
        id: Int,
        decrypt: Boolean
    ): LiveData<List<AttachmentEntity>>

    fun getMessages(contactId: Int): LiveData<List<MessageAttachmentRelation>>

    fun getQuoteId(quoteWebId: String): Int

    fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    suspend fun insertMessage(messageEntity: MessageEntity): Long

    fun insertListMessage(messageEntityList: List<MessageEntity>)

    fun updateMessage(messageEntity: MessageEntity)

    fun existMessage(id: String): Boolean

    suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)

    suspend fun cleanSelectionMessages(contactId: Int)

    suspend fun deleteMessagesSelected(
        contactId: Int,
        listMessageRelations: List<MessageAttachmentRelation>
    )

    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    suspend fun copyMessagesSelected(contactId: Int): List<String>

    suspend fun getLastMessageByContact(contactId: Int): MessageAttachmentRelation

    suspend fun getContactIdByWebId(listWebId: List<String>): Int

    suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>

    suspend fun deleteMessagesByWebId(listWebIdMessages: List<String>)

    suspend fun updateMessageStatus(
        messagesWebIds: List<String>,
        status: Int
    )

    suspend fun getTextMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation>

    fun getMessagesForHome(): LiveData<MutableList<MessageAttachmentRelation>>

    suspend fun getMissedCallsByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    suspend fun deleteMessagesByContactId(contactId: Int)

    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)

    fun deleteMessagesByTotalSelfDestructionAt()

    suspend fun deleteMessageByContactIdAndType(contactId: Int, type: Int)

    suspend fun updateMessageStatusBeforeAttachment(attachmentsWebIds: List<String>)

    suspend fun deleteDuplicateMessage()

    suspend fun addUUIDMessage()

    suspend fun deleteDuplicateAttachment()

    suspend fun addUUIDAttachment()
}