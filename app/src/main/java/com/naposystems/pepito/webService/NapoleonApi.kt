package com.naposystems.pepito.webService

import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.dto.addContact.*
import com.naposystems.pepito.dto.contactUs.ContactUsReqDTO
import com.naposystems.pepito.dto.contactUs.ContactUsResDTO
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.contacts.ContactsResDTO
import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeResDTO
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountQuestionsReqDTO
import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountQuestionsResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.pepito.utility.Constants.NapoleonApi.CREATE_ACCOUNT
import com.naposystems.pepito.utility.Constants.NapoleonApi.DELETE_CONTACT
import com.naposystems.pepito.utility.Constants.NapoleonApi.DELETE_MESSAGES_FOR_ALL
import com.naposystems.pepito.utility.Constants.NapoleonApi.FRIEND_SHIP_SEARCH
import com.naposystems.pepito.utility.Constants.NapoleonApi.FRIEND_SHIP_SEARCH_BY_DATE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GENERATE_CODE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_FRIENDSHIP_REQUESTS
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_FRIENDSHIP_REQUEST_QUANTITY
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_MY_MESSAGES
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_MESSAGE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_RECOVERY_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.PUT_BLOCK_CONTACT
import com.naposystems.pepito.utility.Constants.NapoleonApi.PUT_FRIENDSHIP_REQUEST
import com.naposystems.pepito.utility.Constants.NapoleonApi.PUT_UNBLOCK_CONTACT
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEARCH_USER
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_ANSWERS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_FRIENDSHIP_REQUEST
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_MESSAGES_READ
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_PQRS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.UPDATE_MUTE_CONVERSATION
import com.naposystems.pepito.utility.Constants.NapoleonApi.UPDATE_USER_INFO
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_NICKNAME
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFICATE_CODE
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFY_MESSAGES_READ
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFY_MESSAGES_RECEIVED
import retrofit2.Response
import retrofit2.http.*

interface NapoleonApi {

    @POST(GENERATE_CODE)
    suspend fun generateCode(@Body sendCodeReqDTO: SendCodeReqDTO): Response<SendCodeResDTO>

    @POST(VERIFICATE_CODE)
    suspend fun verificateCode(@Body enterCodeReqDTO: EnterCodeReqDTO): Response<EnterCodeResDTO>

    @POST(VALIDATE_NICKNAME)
    suspend fun validateNickname(@Body validateNicknameReqDTO: ValidateNicknameReqDTO): Response<ValidateNicknameResDTO>

    @POST(CREATE_ACCOUNT)
    suspend fun createAccount(@Body createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>

    @PUT(UPDATE_USER_INFO)
    suspend fun updateUserInfo(@Body updateUserInfoReqDTO: UpdateUserInfoReqDTO): Response<UpdateUserInfoResDTO>

    @PUT(UPDATE_MUTE_CONVERSATION)
    suspend fun updateMuteConversation(@Path("id") idContact: Int, @Body muteConversationReqDTO: MuteConversationReqDTO) : Response<MuteConversationResDTO>

    @GET(FRIEND_SHIP_SEARCH)
    suspend fun getContactsByState(@Path("state") state: String): Response<ContactsResDTO>

    @GET(FRIEND_SHIP_SEARCH_BY_DATE)
    suspend fun getContactsByDate(@Path("state") state: String, @Query("date") date: String): Response<ContactsResDTO>

    @POST(SEND_PQRS)
    suspend fun sendPqrs(@Body contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO>

    @GET(GET_QUESTIONS)
    suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>

    @POST(SEND_QUESTIONS)
    suspend fun sendRecoveryQuestions(@Body registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>

    @POST(SEND_MESSAGE)
    suspend fun sendMessage(@Body messageReqDTO: MessageReqDTO): Response<MessageResDTO>

    @GET(GET_MY_MESSAGES)
    suspend fun getMyMessages(): Response<List<MessageResDTO>>

    @GET(VERIFY_MESSAGES_RECEIVED)
    suspend fun verifyMessagesReceived(): Response<List<String>>

    @POST(VERIFY_MESSAGES_READ)
    suspend fun verifyMessagesRead(): Response<List<String>>

    @PUT(SEND_MESSAGES_READ)
    suspend fun sendMessagesRead(@Body messagesReadReqDTO: MessagesReadReqDTO): Response<List<String>>

    @GET(GET_RECOVERY_QUESTIONS)
    suspend fun getRecoveryQuestions(@Path("nick") nick: String): Response<List<RecoveryAccountResDTO>>

    @POST(SEND_ANSWERS)
    suspend fun sendAnswers(@Body recoveryAccountQuestionsReqDTO: RecoveryAccountQuestionsReqDTO): Response<RecoveryAccountQuestionsResDTO>

    @GET(SEARCH_USER)
    suspend fun searchUser(@Path("nick") nick: String): Response<List<ContactResDTO>>

    @POST(SEND_FRIENDSHIP_REQUEST)
    suspend fun sendFriendshipRequest(@Body friendshipRequestReqDTO: FriendshipRequestReqDTO): Response<FriendshipRequestResDTO>

    @GET(GET_FRIENDSHIP_REQUESTS)
    suspend fun getFriendshipRequests(): Response<FriendshipRequestsResDTO>

    @PUT(PUT_FRIENDSHIP_REQUEST)
    suspend fun putFriendshipRequest(@Path("id") friendshipRequestId: String, @Body request: FriendshipRequestPutReqDTO): Response<FriendshipRequestPutResDTO>

    @GET(GET_FRIENDSHIP_REQUEST_QUANTITY)
    suspend fun getFriendshipRequestQuantity(): Response<FriendshipRequestQuantityResDTO>

    @PUT(PUT_BLOCK_CONTACT)
    suspend fun putBlockContact(@Path("id") blockContact: String): Response<BlockedContactResDTO>

    @PUT(PUT_UNBLOCK_CONTACT)
    suspend fun putUnblockContact(@Path("id") unblockContact: String): Response<UnblockContactResDTO>

    @DELETE(DELETE_CONTACT)
    suspend fun sendDeleteContact(@Path("id") deleteContact: String): Response<DeleteContactResDTO>

    @POST(DELETE_MESSAGES_FOR_ALL)
    suspend fun deleteMessagesForAll(@Body deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>

    @GET(DELETE_MESSAGES_FOR_ALL)
    suspend fun getDeletedMessages(): Response<List<String>>
}