package com.naposystems.napoleonchat.repository.validateNickname

import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.napoleonchat.ui.register.validateNickname.IContractValidateNickname
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class ValidateNicknameRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractValidateNickname.Repository {

    override suspend fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO): Response<ValidateNicknameResDTO> {
        return napoleonApi.validateNickname(validateNicknameReqDTO)
    }
}