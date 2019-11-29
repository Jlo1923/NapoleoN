package com.naposystems.pepito.webService

import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeResDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import com.naposystems.pepito.utility.Constants.NapoleonApi.CREATE_ACCOUNT
import com.naposystems.pepito.utility.Constants.NapoleonApi.GENERATE_CODE
import com.naposystems.pepito.utility.Constants.NapoleonApi.UPDATE_USER_INFO
import com.naposystems.pepito.utility.Constants.NapoleonApi.VALIDATE_NICKNAME
import com.naposystems.pepito.utility.Constants.NapoleonApi.VERIFICATE_CODE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

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
}