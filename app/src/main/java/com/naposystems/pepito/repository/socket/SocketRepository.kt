package com.naposystems.pepito.repository.socket

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.dto.conversation.message.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val conversationLocalDataSource: ConversationDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource
) : IContractSocketService.Repository {

    override fun getMyMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getMyMessages()

            if (response.isSuccessful) {
                val messageResList: MutableList<MessageResDTO> = response.body()!!.toMutableList()

                if (messageResList.isNotEmpty()) {

                    for (messageRes in messageResList) {

                        val message = MessageResDTO.toMessageEntity(
                            0, messageRes, Constants.IsMine.NO.value
                        )

                        val conversationId = messageLocalDataSource.insertMessage(message)

                        val listAttachments = AttachmentResDTO.toListConversationAttachment(
                            conversationId.toInt(),
                            messageRes.attachments
                        )

                        withContext(Dispatchers.IO) {
                            listAttachments.forEach { attachment ->

                                val responseDownloadFile =
                                    napoleonApi.downloadFileByUrl(attachment.body)

                                if (responseDownloadFile.isSuccessful) {
                                    attachment.uri =
                                        saveToDisk(responseDownloadFile.body()!!, attachment)
                                }
                            }
                        }

                        attachmentLocalDataSource.insertAttachments(listAttachments)

                        val unreadMessages =
                            messageResList.filter { it.userDestination == messageRes.userDestination }
                                .size

                        conversationLocalDataSource.insertConversation(
                            messageRes,
                            false,
                            unreadMessages
                        )
                    }

                }
            }
        }
    }

    override fun verifyMessagesReceived() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesReceived()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.UNREAD.status
                )
            }
        }
    }

    override fun verifyMessagesRead() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesRead()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.READED.status
                )
            }
        }
    }

    private fun saveToDisk(body: ResponseBody, attachment: Attachment): String {
        try {

            val path = File(context.externalCacheDir!!, "Audios")
            if (!path.exists())
                path.mkdirs()
            val audioFile = File(path, "${attachment.webId}.mp3")

            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

            val encryptedFile = EncryptedFile.Builder(
                audioFile,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                Timber.d("File Size=" + body.contentLength())
                inputStream = body.byteStream()
                outputStream = encryptedFile.openFileOutput()
                val data = ByteArray(4096)
                var count: Int
                var progress = 0
                while (inputStream.read(data).also { count = it } != -1) {
                    outputStream.write(data, 0, count)
                    progress += count
                    Timber.d(
                        "Progress: " + progress + "/" + body.contentLength() + " >>>> " + progress.toFloat() / body.contentLength()
                    )
                }
                outputStream.flush()
                Timber.d("File saved successfully!")
                return ""
            } catch (e: IOException) {
                e.printStackTrace()
                Timber.d("Failed to save the file!")
                return ""
            } finally {
                inputStream?.close()
                outputStream?.close()
                return audioFile.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Timber.d("Failed to save the file!")
            return ""
        }
    }

    override fun getDeletedMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getDeletedMessages()
            if(response.isSuccessful) {
                if(response.body()!!.count() > 0){
                    val contactId = messageLocalDataSource.getIdContactWithWebId(response.body()!!)
                    messageLocalDataSource.deletedMessages(response.body()!!)
                    when(val messageAndAttachment=  messageLocalDataSource.getLastMessageByContact(contactId)) {
                        null -> {
                            conversationLocalDataSource.cleanConversation(contactId)
                        }
                        else -> {
                            conversationLocalDataSource.getQuantityUnreads(contactId).let { quantityUnreads->
                                if (quantityUnreads > 0) {
                                    conversationLocalDataSource.updateConversationByContact(
                                        contactId,
                                        messageAndAttachment.message.body,
                                        messageAndAttachment.message.createdAt,
                                        messageAndAttachment.message.status,
                                        quantityUnreads - response.body()!!.count())
                                } else {
                                    conversationLocalDataSource.updateConversationByContact(
                                        contactId,
                                        messageAndAttachment.message.body,
                                        messageAndAttachment.message.createdAt,
                                        messageAndAttachment.message.status,
                                        0)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}