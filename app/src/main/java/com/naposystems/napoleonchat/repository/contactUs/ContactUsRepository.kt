package com.naposystems.napoleonchat.repository.contactUs

import com.naposystems.napoleonchat.dto.contactUs.ContactUs422DTO
import com.naposystems.napoleonchat.dto.contactUs.ContactUsErrorDTO
import com.naposystems.napoleonchat.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.dto.contactUs.ContactUsResDTO
import com.naposystems.napoleonchat.ui.contactUs.IContractContactUs
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ContactUsRepository
@Inject constructor(
    private val moshi: Moshi,
    private val napoleonApi: NapoleonApi
    ) :    IContractContactUs.Repository {

    override suspend fun sendPqrs(contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO> {
        return napoleonApi.sendPqrs(contactUsReqDTO)
    }

    override fun get422Error(response: Response<ContactUsResDTO>): List<String> {
        val adapter = moshi.adapter(ContactUs422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(enterCodeError!!)
    }

    override fun getDefaultError(response: Response<ContactUsResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(ContactUsErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }
}