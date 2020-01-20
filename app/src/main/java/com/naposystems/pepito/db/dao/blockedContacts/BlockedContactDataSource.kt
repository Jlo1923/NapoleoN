package com.naposystems.pepito.db.dao.blockedContacts

import com.naposystems.pepito.entity.BlockedContact

interface BlockedContactDataSource {

    fun insertBlockedContacts(blockedContacts: List<BlockedContact>)

    suspend fun getBlockedContacts(): List<BlockedContact>

    suspend fun clearTable()
}