package com.naposystems.pepito.repository.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.conversation.message.Conversation422DTO
import com.naposystems.pepito.dto.conversation.message.ConversationErrorDTO
import com.naposystems.pepito.dto.conversation.message.ConversationReqDTO
import com.naposystems.pepito.dto.conversation.message.ConversationResDTO
import com.naposystems.pepito.dto.conversation.socket.AuthReqDTO
import com.naposystems.pepito.dto.conversation.socket.HeadersReqDTO
import com.naposystems.pepito.dto.conversation.socket.SocketReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.Conversation
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.service.IContractSocketService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val socketService: IContractSocketService,
    private val userLocalDataSource: UserLocalDataSource,
    private val conversationLocalDataSource: ConversationDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
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

    override fun getLocalMessages(
        channelName: String,
        pageSize: Int
    ): LiveData<PagedList<Conversation>> {
        return conversationLocalDataSource.getMessages(channelName, pageSize)
    }

    override suspend fun getRemoteMessages(
        user: User,
        contactId: Int,
        channelName: String
    ) {
        try {
            val response = napoleonApi.getMessages(contactId)

            if (response.isSuccessful) {

                val conversations = ConversationResDTO.toConversationListEntity(
                    response.body()!!,
                    Constants.IsMine.NO.value,
                    channelName
                )

                conversationLocalDataSource.insertListConversation(conversations)
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun sendMessage(conversationReqDTO: ConversationReqDTO): Response<ConversationResDTO> {
        return napoleonApi.sendMessage(conversationReqDTO)
    }

    override suspend fun getLocalUser(): User {
        return userLocalDataSource.getUser(firebaseId)
    }

    override fun insertConversation(conversation: Conversation): Long {
        return conversationLocalDataSource.insertConversation(conversation)
    }

    override fun insertListConversation(conversationList: List<Conversation>) {
        conversationLocalDataSource.insertListConversation(conversationList)
    }

    override fun updateConversation(conversation: Conversation) {
        conversationLocalDataSource.updateConversation(conversation)
    }

    override fun get422Error(response: Response<ConversationResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(Conversation422DTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getError(response: Response<ConversationResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(ConversationErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }
}