package com.naposystems.napoleonchat.ui.changeParams

import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import retrofit2.Response


interface IContractChangeDialogParams {

    interface ViewModel {
        fun updateNameFakeContact(contactId: Int, nameFake: String)
        fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
    }

    interface Repository {
        suspend fun updateNameOrNickNameFakeContact(
            contactId: Int,
            data: String,
            isNameFake: Boolean
        ): Response<ContactFakeResDTO>

        suspend fun updateContactFakeLocal(contactId: Int, contactUpdated: ContactFakeResDTO)
    }

}