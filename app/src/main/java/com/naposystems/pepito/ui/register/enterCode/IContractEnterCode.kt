package com.naposystems.pepito.ui.register.enterCode

import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeResDTO
import retrofit2.Response

interface IContractEnterCode {

    interface ViewModel {
        fun sendCode(enterCodeReqDTO: EnterCodeReqDTO)
    }

    interface Repository {
        suspend fun sendCodeToWs(enterCodeReqDTO: EnterCodeReqDTO): Response<EnterCodeResDTO>
    }
}