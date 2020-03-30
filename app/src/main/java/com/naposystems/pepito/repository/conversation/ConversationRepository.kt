package com.naposystems.pepito.repository.conversation

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessage422DTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesErrorDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.pepito.dto.conversation.message.*
import com.naposystems.pepito.dto.conversation.socket.AuthReqDTO
import com.naposystems.pepito.dto.conversation.socket.HeadersReqDTO
import com.naposystems.pepito.dto.conversation.socket.SocketReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.*
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject


class ConversationRepository @Inject constructor(
    private val context: Context,
    private val socketService: IContractSocketService.SocketService,
    private val userLocalDataSource: UserLocalDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi,
    private val conversationLocalDataSource: ConversationDataSource,
    private val contactDataSource: ContactDataSource,
    private val quoteDataSource: QuoteDataSource
) :
    IContractConversation.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val firebaseId: String by lazy {
        sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override suspend fun subscribeToChannel(userToChat: Contact): String {

        var user: User? = null

        coroutineScope {
            user = getLocalUser()
        }

        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        var minorId: String = userToChat.id.toString()
        var mayorId: String = user!!.id.toString()

        if (user!!.id < userToChat.id) {
            mayorId = userToChat.id.toString()
            minorId = user!!.id.toString()
        }

        val channelName =
            "private-private.${minorId}.${mayorId}"

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.subscribe(SocketReqDTO.toJSONObject(socketReqDTO))

        return channelName
    }

    override fun unSubscribeToChannel(userToChat: Contact, channelName: String) {
        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.unSubscribe(SocketReqDTO.toJSONObject(socketReqDTO), channelName)
    }

    override fun getLocalMessages(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageLocalDataSource.getMessages(contactId)
    }

    override suspend fun getQuoteId(quoteWebId: String): Int {
        return messageLocalDataSource.getQuoteId(quoteWebId)
    }

    override suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override suspend fun sendMessageAttachment(attachment: Attachment): Response<AttachmentResDTO> {

        val requestBodyMessageId = createPartFromString(attachment.messageWebId)
        val requestBodyType = createPartFromString(attachment.type)
        val requestBodyFilePart = createPartFromFile(attachment)

        return napoleonApi.sendMessageAttachment(
            messageId = requestBodyMessageId,
            attachmentType = requestBodyType,
            file = requestBodyFilePart
        )
    }

    override suspend fun sendMessageTest(
        userDestination: Int,
        quoted: String,
        body: String,
        attachmentType: String,
        uriString: String,
        origin: Int
    ): Response<MessageResDTO>? {
        /*val listParts: MutableList<MultipartBody.Part> = ArrayList()

        listParts.add(createPartFromFile(uriString, origin, attachmentType))

        val requestBodyUserDestination = createPartFromString(userDestination.toString())
        val requestBodyQuoted = createPartFromString(quoted)
        val requestBodyBody = createPartFromString(body)
        val requestBodyAttachmentType = createPartFromString(attachmentType)

        return napoleonApi.sendMessageTest(
            userDestination = requestBodyUserDestination,
            quoted = requestBodyQuoted,
            body = requestBodyBody,
            attachmentType = requestBodyAttachmentType,
            files = listParts
        )*/
        return null
    }

    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create(MultipartBody.FORM, string)
    }

    private fun createPartFromFile(attachment: Attachment): MultipartBody.Part {

        val subfolder = FileManager.getSubfolderByAttachmentType(attachmentType = attachment.type)

        val fileUri = Utils.getFileUri(
            context = context, fileName = attachment.uri, subFolder = subfolder
        )

        val file = File(fileUri.path!!)

        val stream = context.contentResolver.openInputStream(fileUri)
        val byteArrayStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)

        var i: Int

        while (stream!!.read(buffer, 0, buffer.size).also { i = it } > 0) {
            byteArrayStream.write(buffer, 0, i)
        }

//        val byteArray = Utils.convertFileInputStreamToByteArray(file.inputStream())
        val byteArray = byteArrayStream.toByteArray()

        val extension = MimeTypeMap.getFileExtensionFromUrl(file.toString())

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(mimeType!!),
            byteArray
        )

        return MultipartBody.Part.createFormData(
            "body",
            "${System.currentTimeMillis()}.$extension",
            requestFile
        )
    }

    private fun getFileFromMediaStore(uri: String): ByteArray {

        val contentUri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentUris.parseId(Uri.parse(uri))
        )

        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(contentUri, "r")

        val fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

        return Utils.convertFileInputStreamToByteArray(fileInputStream)
    }

    private fun getFileFromContentProvider(uriString: String): ByteArray {
        val file = File(uriString)
        val fileInputStream = file.inputStream()

        return Utils.convertFileInputStreamToByteArray(fileInputStream)
    }

    override fun getLocalContact(contactId: Int): LiveData<Contact> {
        return contactDataSource.getContact(contactId)
    }

    override suspend fun getLocalUser(): User {
        return userLocalDataSource.getUser(firebaseId)
    }

    override fun insertMessage(message: Message): Long {
        return messageLocalDataSource.insertMessage(message)
    }

    override fun insertListMessage(messageList: List<Message>) {
        messageLocalDataSource.insertListMessage(messageList)
    }

    override suspend fun insertConversation(messageResDTO: MessageResDTO) {
        conversationLocalDataSource.insertConversation(messageResDTO, true, 0)
    }

    override fun updateMessage(message: Message) {
        messageLocalDataSource.updateMessage(message)
    }

    override suspend fun sendMessagesRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getMessagesByStatus(
                contactId,
                Constants.MessageStatus.UNREAD.status
            )

        if (messagesUnread.isNotEmpty()) {
            try {

                val messagesReadReqDTO = MessagesReadReqDTO(
                    messagesUnread
                )

                val response = napoleonApi.sendMessagesRead(messagesReadReqDTO)

                if (response.isSuccessful) {
                    messageLocalDataSource.updateMessageStatus(
                        response.body()!!,
                        Constants.MessageStatus.READED.status
                    )

                    conversationLocalDataSource.updateConversation(contactId)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun insertAttachment(attachment: Attachment): Long {
        return attachmentLocalDataSource.insertAttachment(attachment)
    }

    override fun insertAttachments(listAttachment: List<Attachment>): List<Long> {
        return attachmentLocalDataSource.insertAttachments(listAttachment)
    }

    override fun updateAttachment(attachment: Attachment) {
        attachmentLocalDataSource.updateAttachment(attachment)
    }

    override fun insertQuote(quoteWebId: String, message: Message) {

        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId)

        var firstAttachment: Attachment? = null

        if (originalMessage.attachmentList.isNotEmpty()) {
            firstAttachment = originalMessage.attachmentList.first()
        }

        val quote = Quote(
            id = 0,
            messageId = message.id,
            contactId = originalMessage.message.contactId,
            body = originalMessage.message.body,
            attachmentType = firstAttachment?.type ?: "",
            thumbnailUri = firstAttachment?.uri ?: "",
            messageParentId = originalMessage.message.id,
            isMine = originalMessage.message.isMine
        )

        quoteDataSource.insertQuote(quote)
    }

    override suspend fun updateStateSelectionMessage(
        contactId: Int,
        idMessage: Int,
        isSelected: Int
    ) {
        messageLocalDataSource.updateStateSelectionMessage(contactId, idMessage, isSelected)
    }

    override suspend fun cleanSelectionMessages(contactId: Int) {
        messageLocalDataSource.cleanSelectionMessages(contactId)
    }

    override suspend fun deleteMessagesSelected(
        contactId: Int,
        listMessages: List<MessageAndAttachment>
    ) {
        messageLocalDataSource.deleteMessagesSelected(contactId, listMessages)
        val messageAndAttachment = messageLocalDataSource.getLastMessageByContact(contactId)
        if (messageAndAttachment != null) {
            conversationLocalDataSource.updateConversationByContact(
                contactId,
                messageAndAttachment.message.body,
                messageAndAttachment.message.createdAt,
                messageAndAttachment.message.status,
                0
            )
        } else {
            conversationLocalDataSource.cleanConversation(contactId)
        }
    }

    override suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO> {
        return napoleonApi.deleteMessagesForAll(deleteMessagesReqDTO)
    }

    override suspend fun copyMessagesSelected(contactId: Int): List<String> {
        return messageLocalDataSource.copyMessagesSelected(contactId)
    }

    override suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>> {
        return messageLocalDataSource.getMessagesSelected(contactId)
    }

    override fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(Message422DTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(MessageErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }

    override fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessage422DTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(DeleteMessagesErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }
}