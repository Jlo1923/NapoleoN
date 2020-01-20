package com.naposystems.pepito.repository.sendCode

import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.dto.sendCode.SendCodeResDTO
import com.naposystems.pepito.ui.register.sendCode.IContractSendCode
import com.naposystems.pepito.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class SendCodeRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractSendCode.Repository {

    override suspend fun requestCode(sendCodeReqDTO: SendCodeReqDTO): Response<SendCodeResDTO> {
        return napoleonApi.generateCode(sendCodeReqDTO)
    }
}