package com.naposystems.pepito.ui.changeFakes

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface IContractChangeFakes {

    interface ViewModel {
        fun updateNameFakeContact(contactId: Int, nameFake: String)
        fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
    }

    interface Repository {
        suspend fun updateNameFakeContact(contactId: Int, nameFake: String)
        suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
    }

}