package com.naposystems.napoleonchat.ui.register.validateNickname

import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateNickname.ValidateNicknameResDTO
import retrofit2.Response

interface IContractValidateNickname {

    interface ViewModel {
        fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO)
        fun setNoValidNickname()
    }

    interface Repository {
        suspend fun validateNickname(validateNicknameReqDTO: ValidateNicknameReqDTO):
                Response<ValidateNicknameResDTO>
    }
}