package com.naposystems.pepito.utility.sharedViewModels.contactRepository

interface IContractContactRepositoryShare {

    interface ViewModel {
        fun getContacts()
    }

    interface Repository {
        suspend fun getContacts()
    }

}