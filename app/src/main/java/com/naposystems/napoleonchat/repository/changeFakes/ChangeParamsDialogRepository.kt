package com.naposystems.napoleonchat.repository.changeFakes

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import com.naposystems.napoleonchat.ui.changeParams.IContractChangeDialogParams
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ChangeParamsDialogRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractChangeDialogParams.Repository {

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
            contact.displayName = contactUpdated.fullname
            contact.nickname = contactUpdated.nickname
            contact.imageUrl = contactUpdated.avatar ?: ""
            contact.displayNameFake =
                if (contactUpdated.fullNameFake.isNullOrEmpty()) contactUpdated.fullname else contactUpdated.fullNameFake
            contact.nicknameFake =
                if (contactUpdated.nicknameFake.isNullOrEmpty()) contactUpdated.nickname else contactUpdated.nicknameFake
            contact.imageUrlFake =
                if (contactUpdated.avatarFake.isNullOrEmpty()) contactUpdated.avatar
                    ?: "" else contactUpdated.avatarFake
            contactLocalDataSource.updateContact(contact)
        }

    }
}