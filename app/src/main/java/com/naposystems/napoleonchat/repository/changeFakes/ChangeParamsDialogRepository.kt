package com.naposystems.napoleonchat.repository.changeFakes

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.ui.changeParams.IContractChangeDialogParams
import javax.inject.Inject

class ChangeParamsDialogRepository@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractChangeDialogParams.Repository {

    override suspend fun updateNameFakeContact(contactId: Int, nameFake: String) {
        contactLocalDataSource.updateNameFakeContact(contactId, nameFake)
    }

    override suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        contactLocalDataSource.updateNicknameFakeContact(contactId, nicknameFake)
    }
}