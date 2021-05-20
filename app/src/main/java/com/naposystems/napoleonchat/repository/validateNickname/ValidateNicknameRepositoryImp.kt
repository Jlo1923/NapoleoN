package com.naposystems.napoleonchat.repository.validateNickname

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameResDTO
import retrofit2.Response
import javax.inject.Inject

class ValidateNicknameRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi
) : ValidateNicknameRepository {

    override suspend fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO): Response<ValidateNicknameResDTO> {
        return napoleonApi.validateNickname(validateNicknameReqDTO)
    }
}