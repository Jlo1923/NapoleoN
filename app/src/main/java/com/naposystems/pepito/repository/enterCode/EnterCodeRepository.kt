package com.naposystems.pepito.repository.enterCode

import com.naposystems.pepito.dto.enterCode.EnterCode422DTO
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.dto.enterCode.EnterCodeResDTO
import com.naposystems.pepito.ui.register.enterCode.IContractEnterCode
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.NapoleonRetrofit
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class EnterCodeRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractEnterCode.Repository {

    override suspend fun sendCodeToWs(enterCodeReqDTO: EnterCodeReqDTO): Response<EnterCodeResDTO> {
        return napoleonApi.verificateCode(enterCodeReqDTO)
    }

    fun get422Error(response: Response<EnterCodeResDTO>): ArrayList<String> {
        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(EnterCode422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        if (enterCodeError!!.firebaseId.isNotEmpty())
            errorList.add(enterCodeError.firebaseId[0])

        if (enterCodeError.codeVerification.isNotEmpty())
            errorList.add(enterCodeError.codeVerification[0])

        return errorList
    }
}