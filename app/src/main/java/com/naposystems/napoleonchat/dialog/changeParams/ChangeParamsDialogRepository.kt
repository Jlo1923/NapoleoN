package com.naposystems.napoleonchat.dialog.changeParams

import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import retrofit2.Response


interface ChangeParamsDialogRepository {
    suspend fun updateNameOrNickNameFakeContact(
        contactId: Int,
        data: String,
        isNameFake: Boolean
    ): Response<ContactFakeResDTO>

    suspend fun updateContactFakeLocal(contactId: Int, contactUpdated: ContactFakeResDTO)
}