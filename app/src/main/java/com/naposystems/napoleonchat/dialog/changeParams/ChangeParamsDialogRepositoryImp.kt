package com.naposystems.napoleonchat.dialog.changeParams

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import retrofit2.Response
import javax.inject.Inject

class ChangeParamsDialogRepositoryImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource
) : ChangeParamsDialogRepository {

    override suspend fun updateNameOrNickNameFakeContact(
        contactId: Int,
        data: String,
        isNameFake: Boolean
    ): Response<ContactFakeResDTO> {
        val request = if (isNameFake) {
            ContactFakeReqDTO(data, null, null)
        } else {
            ContactFakeReqDTO(null, data, null)
        }
        return napoleonApi.updateContactFake(request, contactId)
    }

    override suspend fun updateContactFakeLocal(contactId: Int, contactUpdated: ContactFakeResDTO) {
        val contact = contactLocalDataSource.getContactById(contactId)
        contact?.let {
            it.displayName = contactUpdated.fullname
            it.nickname = contactUpdated.nickname
            it.imageUrl = contactUpdated.avatar ?: ""
            it.displayNameFake =
                if (contactUpdated.fullNameFake.isNullOrEmpty()) contactUpdated.fullname else contactUpdated.fullNameFake
            it.nicknameFake =
                if (contactUpdated.nicknameFake.isNullOrEmpty()) contactUpdated.nickname else contactUpdated.nicknameFake
            it.imageUrlFake =
                if (contactUpdated.avatarFake.isNullOrEmpty()) contactUpdated.avatar
                    ?: "" else contactUpdated.avatarFake
            contactLocalDataSource.updateContact(it)
        }

    }
}