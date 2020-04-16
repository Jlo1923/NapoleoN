package com.naposystems.pepito.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface IContractContactProfileShare {

    interface ViewModel {
        fun getLocalContact(contactId : Int)
    }

    interface Repository {
        fun getLocalContact(contactId : Int): LiveData<Contact>
    }

}