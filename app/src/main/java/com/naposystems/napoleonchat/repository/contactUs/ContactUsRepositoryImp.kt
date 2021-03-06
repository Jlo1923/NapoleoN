package com.naposystems.napoleonchat.repository.contactUs

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsUnprocessableEntityDTO
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ContactUsRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi
) : ContactUsRepository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendPqrs(contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO> {
        return napoleonApi.sendPqrs(contactUsReqDTO)
    }

    override fun getUnprocessableEntityError(response: Response<ContactUsResDTO>): List<String> {
        val adapter = moshi.adapter(ContactUsUnprocessableEntityDTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.getUnprocessableEntityErrors(enterCodeError!!)
    }

    override fun getDefaultError(response: Response<ContactUsResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(ContactUsErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }
}