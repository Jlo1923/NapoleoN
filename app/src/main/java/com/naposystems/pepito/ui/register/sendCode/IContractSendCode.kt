package com.naposystems.pepito.ui.register.sendCode

import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import kotlinx.coroutines.Deferred
import retrofit2.Response

interface IContractSendCode {

    interface ViewModel {
        fun requestCode(sendCodeReqDTO: SendCodeReqDTO)
    }

    interface Repository {
        suspend fun requestCode(sendCodeReqDTO: SendCodeReqDTO) : Response<SendCodeResDTO>
    }
}