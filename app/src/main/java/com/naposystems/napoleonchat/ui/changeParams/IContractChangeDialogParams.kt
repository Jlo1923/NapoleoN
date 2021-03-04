package com.naposystems.napoleonchat.ui.changeParams


interface IContractChangeDialogParams {

    interface ViewModel {
        fun updateNameFakeContact(contactId: Int, nameFake: String)
        fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
    }

    interface Repository {
        suspend fun updateNameFakeContact(contactId: Int, nameFake: String)
        suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)
    }

}