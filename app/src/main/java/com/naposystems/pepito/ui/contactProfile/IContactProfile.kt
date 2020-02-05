package com.naposystems.pepito.ui.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface IContactProfile {

    interface ViewModel {
        fun getLocalContact(idContact : Int)
    }

    interface Repository {
        fun getLocalContact(idContact : Int): LiveData<Contact>
    }

}