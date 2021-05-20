package com.naposystems.napoleonchat.repository.validateNickname

import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameResDTO
import retrofit2.Response

interface ValidateNicknameRepository {
    suspend fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO):
            Response<ValidateNicknameResDTO>
}