package com.naposystems.pepito.ui.register.validateNickname

import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameResDTO
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