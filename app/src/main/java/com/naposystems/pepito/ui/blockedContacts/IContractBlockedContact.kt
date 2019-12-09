package com.naposystems.pepito.ui.blockedContacts

import com.naposystems.pepito.dto.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.entity.BlockedContact
import retrofit2.Response

interface IContractBlockedContact {

    interface ViewModel {
        fun getBlockedContacts()
    }

    interface Repository {
        suspend fun getBlockedContacts(): List<BlockedContact>
    }
}