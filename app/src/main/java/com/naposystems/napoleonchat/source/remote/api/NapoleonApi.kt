package com.naposystems.napoleonchat.source.remote.api

import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogResDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.*
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.CallContactReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.CallContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.readyForCall.ReadyForCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.readyForCall.ReadyForCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.*
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeResDTO
import com.naposystems.napoleonchat.source.remote.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.napoleonchat.source.remote.dto.language.UserLanguageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageAndAttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageReceivedResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountQuestionsReqDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountQuestionsResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeResDTO
import com.naposystems.napoleonchat.source.remote.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.*
import com.naposystems.napoleonchat.source.remote.dto.user.LogoutResDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.BLOCK_ATTACKER
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.CALL_CONTACT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.CANCEL_CALL
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.CANCEL_SUBSCRIPTION
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.CHECK_SUBSCRIPTION
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.CREATE_ACCOUNT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.DELETE_CONTACT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.DELETE_MESSAGES_FOR_ALL
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.FRIEND_SHIP_SEARCH
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.FRIEND_SHIP_SEARCH_BY_DATE
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GENERATE_CODE
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_FRIENDSHIP_REQUESTS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_FRIENDSHIP_REQUESTS_RECEIVED
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_FRIENDSHIP_REQUEST_QUANTITY
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_MY_MESSAGES
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_QUESTIONS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_RECOVERY_QUESTIONS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.GET_SUBSCRIPTION_USER
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.LOG_OUT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.NOTIFY_MESSAGE_RECEIVED
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.PUT_BLOCK_CONTACT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.PUT_FRIENDSHIP_REQUEST
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.PUT_UNBLOCK_CONTACT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.READY_CALL
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.REJECT_CALL
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEARCH_USER
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_ANSWERS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_FRIENDSHIP_REQUEST
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_MESSAGE
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_MESSAGES_READ
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_MESSAGE_ATTACHMENT
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_MESSAGE_TEST
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_PQRS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_QUESTIONS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.SEND_SELECTED_SUBSCRIPTION
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.TYPE_SUBSCRIPTIONS
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.UPDATE_CONTACT_FAKE
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.UPDATE_MUTE_CONVERSATION
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.UPDATE_USER_INFO
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.VALIDATE_NICKNAME
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.VERIFICATE_CODE
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.VERIFY_MESSAGES_READ
import com.naposystems.napoleonchat.utility.Constants.NapoleonApi.VERIFY_MESSAGES_RECEIVED
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NapoleonApi {

    @Streaming
    @GET
    suspend fun downloadFileByUrl(@Url fileUrl: String): Response<ResponseBody>

    @POST(GENERATE_CODE)
    suspend fun generateCode(@Body sendCodeReqDTO: SendCodeReqDTO): Response<SendCodeResDTO>

    @POST(VERIFICATE_CODE)
    suspend fun verificateCode(@Body enterCodeReqDTO: EnterCodeReqDTO): Response<EnterCodeResDTO>

    @POST(VALIDATE_NICKNAME)
    suspend fun validateNickname(@Body validateNicknameReqDTO: ValidateNicknameReqDTO): Response<ValidateNicknameResDTO>

    @POST(CREATE_ACCOUNT)
    suspend fun createAccount(@Body createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>

    @PUT(UPDATE_USER_INFO)
    suspend fun updateUserInfo(@Body updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO>

    @PUT(UPDATE_USER_INFO)
    suspend fun updateUserStatus(@Body userStatusReqDTO: UserStatusReqDTO): Response<UpdateUserInfoResDTO>

    @PUT(UPDATE_USER_INFO)
    suspend fun updateUserLanguage(@Body userStatusReqDTO: UserLanguageReqDTO): Response<UpdateUserInfoResDTO>

    @PUT(UPDATE_MUTE_CONVERSATION)
    suspend fun updateMuteConversation(
        @Path("id") idContact: Int,
        @Body muteConversationReqDTO: MuteConversationReqDTO
    ): Response<MuteConversationResDTO>

    @GET(FRIEND_SHIP_SEARCH)
    suspend fun getContactsByState(@Path("state") state: String): Response<ContactsResDTO>

    @GET(FRIEND_SHIP_SEARCH_BY_DATE)
    suspend fun getContactsByDate(
        @Path("state") state: String,
        @Query("date") date: String
    ): Response<ContactsResDTO>

    @POST(SEND_PQRS)
    suspend fun sendPqrs(@Body contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO>

    @GET(GET_QUESTIONS)
    suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>

    @POST(SEND_QUESTIONS)
    suspend fun sendRecoveryQuestions(@Body registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>

    @POST(SEND_MESSAGE)
    suspend fun sendMessage(@Body messageReqDTO: MessageReqDTO): Response<MessageResDTO>

    @Multipart
    @POST(SEND_MESSAGE_ATTACHMENT)
    suspend fun sendMessageAttachment(
        @Part("message_id") messageId: RequestBody,
        @Part("type") attachmentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("destroy") destroy: RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<AttachmentResDTO>

    @Multipart
    @POST(SEND_MESSAGE_TEST)
    suspend fun sendMessageTest(
        @Part("user_receiver") userDestination: RequestBody,
        @Part("quoted") quoted: RequestBody,
        @Part("body") body: RequestBody,
        @Part("type_attachment") attachmentType: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<MessageResDTO>

    @GET(GET_MY_MESSAGES)
    suspend fun getMyMessages(): Response<List<MessageResDTO>>

    @GET(VERIFY_MESSAGES_RECEIVED)
    suspend fun verifyMessagesReceived(): Response<MessageAndAttachmentResDTO>

    @POST(VERIFY_MESSAGES_READ)
    suspend fun verifyMessagesRead(): Response<MessageAndAttachmentResDTO>

    @PUT(SEND_MESSAGES_READ)
    suspend fun sendMessagesRead(@Body messagesReqDTO: MessagesReqDTO): Response<MessagesResDTO>

    @POST(NOTIFY_MESSAGE_RECEIVED)
    suspend fun notifyMessageReceived(@Body messageReceivedReqDTO: MessagesReqDTO): Response<MessageReceivedResDTO>

    @GET(GET_RECOVERY_QUESTIONS)
    suspend fun getRecoveryQuestions(@Path("nick") nick: String): Response<RecoveryAccountUserTypeResDTO>

    @POST(SEND_ANSWERS)
    suspend fun sendAnswers(@Body recoveryAccountQuestionsReqDTO: RecoveryAccountQuestionsReqDTO): Response<RecoveryAccountQuestionsResDTO>

    @GET(SEARCH_USER)
    suspend fun searchUser(@Path("nick") nick: String): Response<List<ContactResDTO>>

    @POST(SEND_FRIENDSHIP_REQUEST)
    suspend fun sendFriendshipRequest(@Body friendshipRequestReqDTO: FriendshipRequestReqDTO): Response<FriendshipRequestResDTO>

    @GET(GET_FRIENDSHIP_REQUESTS)
    suspend fun getFriendshipRequests(): Response<FriendshipRequestsResDTO>

    @GET(GET_FRIENDSHIP_REQUESTS_RECEIVED)
    suspend fun getFriendShipRequestReceivedHome(): Response<List<FriendshipRequestReceivedDTO>>

    @PUT(PUT_FRIENDSHIP_REQUEST)
    suspend fun putFriendshipRequest(
        @Path("id") friendshipRequestId: String,
        @Body request: FriendshipRequestPutReqDTO
    ): Response<FriendshipRequestPutResDTO>

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
    suspend fun getDeletedMessages(): Response<MessageAndAttachmentResDTO>

    @POST(BLOCK_ATTACKER)
    suspend fun blockAttacker(@Body accountAttackDialogReqDTO: AccountAttackDialogReqDTO): Response<AccountAttackDialogResDTO>

    @GET(GET_SUBSCRIPTION_USER)
    suspend fun getSubscriptionUser(): Response<SubscriptionUserResDTO>

    @GET(TYPE_SUBSCRIPTIONS)
    suspend fun typeSubscriptions(): Response<List<SubscriptionsResDTO>>

    @POST(SEND_SELECTED_SUBSCRIPTION)
    suspend fun sendSelectedSubscription(@Body typeSubscriptionReqDTO: TypeSubscriptionReqDTO): Response<SubscriptionUrlResDTO>

    @POST(CALL_CONTACT)
    suspend fun callContact(@Body callContactReqDTO: CallContactReqDTO): Response<CallContactResDTO>

    @POST(REJECT_CALL)
    suspend fun rejectCall(@Body rejectCallReqDTO: RejectCallReqDTO): Response<Any>

    @POST(LOG_OUT)
    suspend fun logout(): Response<LogoutResDTO>

    @POST(CANCEL_SUBSCRIPTION)
    suspend fun cancelSubscription(): Response<CancelSubscriptionResDTO>

    @GET(CHECK_SUBSCRIPTION)
    suspend fun checkSubscription(): Response<StateSubscriptionResDTO>

    @POST(CANCEL_CALL)
    suspend fun cancelCall(@Body cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO>

    @POST(READY_CALL)
    suspend fun readyForCall(@Body readyForCallReqDTO: ReadyForCallReqDTO): Response<ReadyForCallResDTO>

    @PUT(UPDATE_CONTACT_FAKE)
    suspend fun updateContactFake(@Body contactFakeReqDTO: ContactFakeReqDTO,@Path("friendshipId") idContact: Int): Response<ContactFakeResDTO>
}