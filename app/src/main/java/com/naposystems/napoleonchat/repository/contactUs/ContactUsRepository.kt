package com.naposystems.napoleonchat.repository.contactUs

import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsResDTO
import retrofit2.Response

interface ContactUsRepository {
        suspend fun sendPqrs(contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO>
        fun getUnprocessableEntityError(response: Response<ContactUsResDTO>): List<String>
        fun getDefaultError(response: Response<ContactUsResDTO>): List<String>
}