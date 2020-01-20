package com.naposystems.pepito.webService

import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.dto.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contactUs.ContactUsReqDTO
import com.naposystems.pepito.dto.contactUs.ContactUsResDTO
import com.naposystems.pepito.dto.contacts.ContactsResDTO
import com.naposystems.pepito.dto.conversation.message.ConversationReqDTO
import com.naposystems.pepito.dto.conversation.message.ConversationResDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeResDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.pepito.utility.Constants.NapoleonApi.CREATE_ACCOUNT
import com.naposystems.pepito.utility.Constants.NapoleonApi.FRIEND_SHIP_SEARCH
import com.naposystems.pepito.utility.Constants.NapoleonApi.GENERATE_CODE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_MESSAGES
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_MESSAGE
import com.naposystems.pepito.utility.Constants.NapoleonApi.GET_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_PQRS
import com.naposystems.pepito.utility.Constants.NapoleonApi.SEND_QUESTIONS
import com.naposystems.pepito.utility.Constants.NapoleonApi.UPDATE_USER_INFO
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_NICKNAME
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFICATE_CODE
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

    @GET(FRIEND_SHIP_SEARCH)
    suspend fun getBlockedContacts(@Path("state") state: String): Response<List<BlockedContactResDTO>>

    @POST(SEND_PQRS)
    suspend fun sendPqrs(@Body contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO>

    @GET(GET_QUESTIONS)
    suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>

    @POST(SEND_QUESTIONS)
    suspend fun sendRecoveryQuestions(@Body registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>

    @GET(FRIEND_SHIP_SEARCH)
    suspend fun getFriendShipSearch(@Path("state") state: String): Response<List<ContactsResDTO>>

    @POST(SEND_MESSAGE)
    suspend fun sendMessage(@Body conversationReqDTO: ConversationReqDTO): Response<ConversationResDTO>

    @GET(GET_MESSAGES)
    suspend fun getMessages(@Path("contact_id") contactId: Int): Response<List<ConversationResDTO>>
}