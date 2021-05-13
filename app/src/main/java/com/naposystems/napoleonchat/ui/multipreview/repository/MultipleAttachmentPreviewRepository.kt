package com.naposystems.napoleonchat.ui.multipreview.repository

import android.content.Context
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.MessageStatus.SENT
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.extensions.getMessageEntityForCreate
import com.naposystems.napoleonchat.utility.extensions.isVideo
import com.naposystems.napoleonchat.utility.extensions.toAttachmentEntityWithFile
import com.naposystems.napoleonchat.utility.extensions.toMessageReqDto
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class MultipleAttachmentPreviewRepository @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val repository: IContractConversation.Repository,
    private val context: Context
) : IContractMultipleAttachmentPreview.Repository {

    override suspend fun insertMessageToContact(message: ItemMessage): MessageEntity {
        val messageToInsert = message.getMessageEntityForCreate()
        val idMessage = repository.insertMessage(messageToInsert).toInt()
        messageToInsert.id = idMessage
        return messageToInsert
    }

    override fun deleteMessageNotSent(id: Int) = repository.deleteMessageNotSent(id)

    override suspend fun insertAttachmentsWithMsgId(
        listFiles: MutableList<MultipleAttachmentFileItem>,
        messageId: Int
    ): List<AttachmentEntity?> {
        val attachments = listFiles.map { multipleAttachmentFile ->
            val file = getFileFromFileItem(multipleAttachmentFile)
            file?.let {
                multipleAttachmentFile.toAttachmentEntityWithFile(
                    it,
                    multipleAttachmentFile.selfDestruction
                )
            }
        }
        attachments.forEach {
            it?.let {
                it.messageId = messageId
                val attachmentId = repository.insertAttachment(it)
                it.id = attachmentId.toInt()
            }
        }
        return attachments
    }

    override suspend fun sendMessage(messageEntity: MessageEntity): Pair<MessageEntity?, String>? {

        try {
            val messageReqDTO = messageEntity.toMessageReqDto(cryptoMessage)
            val messageResponse = repository.sendMessage(messageReqDTO)

            if (messageResponse.isSuccessful) {
                return Pair(
                    MessageResDTO.toMessageEntity(
                        messageEntity,
                        messageResponse.body()!!,
                        Constants.IsMine.YES.value
                    ).apply {
                        status = SENT.status
                    },
                    messageResponse.body()?.id ?: ""
                )
            }
        } catch (exception: Exception) {
            messageEntity.status = Constants.MessageStatus.ERROR.status
            repository.updateMessage(messageEntity, false)
            return null
        }
        return null
    }

    private suspend fun getFileFromFileItem(item: MultipleAttachmentFileItem): File? {
        item.contentUri?.let {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(it, "r")
            val fileInputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
            return if (item.isVideo()) {
                videoFile(fileInputStream)
            } else {
                imageFile(fileInputStream)
            }
        } ?: run { return null }
    }

    private suspend fun videoFile(fileInputStream: FileInputStream) = FileManager.copyFile(
        context,
        fileInputStream,
        Constants.CacheDirectories.VIDEOS.folder,
        "${System.currentTimeMillis()}.mp4"
    )

    private suspend fun imageFile(fileInputStream: FileInputStream) =
        FileManager.compressImageFromFileInputStream(context, fileInputStream)
}


