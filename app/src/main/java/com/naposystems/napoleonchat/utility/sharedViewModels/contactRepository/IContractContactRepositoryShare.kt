package com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository

interface IContractContactRepositoryShare {

    interface ViewModel {
        fun getContacts(state : String, location : Int)
    }

    interface Repository {
        suspend fun getContacts(state : String, location : Int) : Boolean
    }

}