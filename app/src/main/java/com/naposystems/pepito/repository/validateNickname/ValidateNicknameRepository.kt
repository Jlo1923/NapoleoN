package com.naposystems.pepito.repository.validateNickname

import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameResDTO
import com.naposystems.pepito.ui.register.validateNickname.IContractValidateNickname
import com.naposystems.pepito.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class ValidateNicknameRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractValidateNickname.Repository {

    override suspend fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO): Response<ValidateNicknameResDTO> {
        return napoleonApi.validateNickname(validateNicknameReqDTO)
    }
}