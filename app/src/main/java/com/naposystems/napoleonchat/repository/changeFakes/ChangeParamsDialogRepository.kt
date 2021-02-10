package com.naposystems.napoleonchat.repository.changeFakes

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.ui.changeParams.IContractChangeDialogParams
import javax.inject.Inject

class ChangeParamsDialogRepository@Inject constructor(
    private val contactDataSource: ContactDataSource
) : IContractChangeDialogParams.Repository {

    override suspend fun updateNameFakeContact(contactId: Int, nameFake: String) {
        contactDataSource.updateNameFakeContact(contactId, nameFake)
    }

    override suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        contactDataSource.updateNicknameFakeContact(contactId, nicknameFake)
    }
}