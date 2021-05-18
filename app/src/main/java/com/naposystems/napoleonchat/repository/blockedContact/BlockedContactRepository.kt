package com.naposystems.napoleonchat.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface BlockedContactRepository {
        suspend fun getBlockedContactsLocal(): LiveData<List<ContactEntity>>
}