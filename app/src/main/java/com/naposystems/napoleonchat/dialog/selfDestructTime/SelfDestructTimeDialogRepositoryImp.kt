package com.naposystems.napoleonchat.dialog.selfDestructTime

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.WAS_IN_PREVIEW
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.extras.IDS_TO_DELETE
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class SelfDestructTimeDialogRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val napoleonApi: NapoleonApi,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val syncManager: SyncManager
) : SelfDestructTimeDialogRepository {

    override fun getSelfDestructTime(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME)
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME,
            selfDestructTime
        )
    }

    override suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        messageLocalDataSource.setSelfDestructTimeByMessages(selfDestructTime, contactId)
        contactLocalDataSource.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int): LiveData<Int> {
        return contactLocalDataSource.getSelfDestructTimeByContact(contactId)
    }

    override suspend fun getSelfDestructTimeAsIntByContact(contactId: Int): Int {
        return contactLocalDataSource.getSelfDestructTimeAsIntByContact(contactId)
    }

    override fun getMessageSelfDestructTimeNotSent(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
        )
    }

    override suspend fun sentAttachmentReaded(fileItem: MultipleAttachmentFileItem) {
        try {
            fileItem.messageAndAttachment?.let {
                val messagesReqDTO = MessagesReqDTO(
                    messages = listOf(
                        MessageDTO(
                            id = it.attachment.webId,
                            status = Constants.StatusMustBe.READED.status,
                            type = Constants.MessageType.ATTACHMENT.type,
                            user = it.contactId
                        )
                    )
                )

                val response = napoleonApi.sendMessagesRead(messagesReqDTO)

                if (response.isSuccessful) {
                    attachmentLocalDataSource.updateAttachmentStatus(
                        listOf(it.attachment.webId),
                        Constants.AttachmentStatus.READED.status
                    )
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun deleteAttachmentLocally(webId: String): Boolean {
        return try {
            attachmentLocalDataSource.deletedAttachments(listOf(webId))
            true
        } catch (ex: Exception) {
            Timber.e(ex)
            false
        }
    }

    override suspend fun deleteMessagesForAll(
        objectForDelete: DeleteMessagesReqDTO
    ): Response<DeleteMessagesResDTO> = napoleonApi.deleteMessagesForAll(objectForDelete)

    override fun saveDeleteFilesInCache(toList: List<MultipleAttachmentFileItem>) {
        val map = toList.map { it.id.toString() }
        sharedPreferencesManager.putStringSet(IDS_TO_DELETE, map.toSet())
    }

    override fun updateAttachments(attachmentsWithWebId: List<AttachmentEntity?>) {
        attachmentsWithWebId.forEach {
            it?.let {
                attachmentLocalDataSource.updateAttachment(it)
            }
        }
    }

    override fun tryMarkMessageParentAsRead(webId: String) {
        syncManager.tryMarkMessageParentAsRead(listOf(webId))
    }

    override fun markWasInPreviewActivity() {
        sharedPreferencesManager.putBoolean(WAS_IN_PREVIEW, true)
    }

}