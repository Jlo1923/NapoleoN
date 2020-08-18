package com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository

interface IContractContactRepositoryShare {

    interface ViewModel {
        fun getContacts()
    }

    interface Repository {
        suspend fun getContacts(): Boolean
    }

}