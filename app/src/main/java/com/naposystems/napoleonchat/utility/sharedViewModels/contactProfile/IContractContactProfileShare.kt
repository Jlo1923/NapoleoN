package com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.entity.Contact

interface IContractContactProfileShare {

    interface ViewModel {
        fun getLocalContact(contactId : Int)
    }

    interface Repository {
        fun getLocalContact(contactId : Int): LiveData<Contact>
    }

}