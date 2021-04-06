package com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface IContractContactProfileShare {

    interface ViewModel {
        fun getLocalContact(contactId : Int)
    }

    interface Repository {
        fun getLocalContact(contactId : Int): LiveData<ContactEntity>
    }

}