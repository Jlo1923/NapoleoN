package com.naposystems.pepito.ui.contactUs

import com.naposystems.pepito.dto.contactUs.ContactUsReqDTO
import com.naposystems.pepito.dto.contactUs.ContactUsResDTO
import retrofit2.Response

interface IContractContactUs {
    interface ViewModel {
        fun sendPqrs(contactUsReqDTO: ContactUsReqDTO)
    }

    interface Repository {
        suspend fun sendPqrs(contactUsReqDTO: ContactUsReqDTO): Response<ContactUsResDTO>
        fun get422Error(response: Response<ContactUsResDTO>): List<String>
        fun getDefaultError(response: Response<ContactUsResDTO>): List<String>
    }
}