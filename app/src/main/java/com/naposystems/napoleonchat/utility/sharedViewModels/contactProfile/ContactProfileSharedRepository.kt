package com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface ContactProfileSharedRepository {

    fun getLocalContact(contactId: Int): LiveData<ContactEntity>

}