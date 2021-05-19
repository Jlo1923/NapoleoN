package com.naposystems.napoleonchat.ui.selfDestructTime

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import retrofit2.Response

interface IContractSelfDestructTime {

    interface ViewModel {

        fun getSelfDestructTime()

        fun setSelfDestructTime(selfDestructTime: Int)

        fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

        fun getSelfDestructTimeByContact(contactId: Int)

        fun getMessageSelfDestructTimeNotSent()

    }

    interface Repository {

        fun getSelfDestructTime(): Int

        fun setSelfDestructTime(selfDestructTime: Int)

        suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

        suspend fun getSelfDestructTimeByContact(contactId: Int): LiveData<Int>

        suspend fun getSelfDestructTimeAsIntByContact(contactId: Int): Int

        fun getMessageSelfDestructTimeNotSent(): Int

        suspend fun sentAttachmentReaded(fileItem: MultipleAttachmentFileItem)

        suspend fun deleteAttachmentLocally(webId: String): Boolean

        suspend fun deleteMessagesForAll(
            objectForDelete: DeleteMessagesReqDTO
        ): Response<DeleteMessagesResDTO>

        fun saveDeleteFilesInCache(toList: List<MultipleAttachmentFileItem>)

        fun updateAttachments(attachmentsWithWebId: List<AttachmentEntity?>)

        fun tryMarkMessageParentAsRead(webId: String)
        fun markWasInPreviewActivity()

    }
}