package com.naposystems.pepito.db.dao.blockedContacts

import com.naposystems.pepito.entity.BlockedContact
import javax.inject.Inject

class BlockedContactsLocalDataSource @Inject constructor(
    private val blockedContactDao: BlockedContactDao
) :
    BlockedContactDataSource {

    override fun insertBlockedContacts(blockedContacts: List<BlockedContact>) {
        return blockedContactDao.insertBlockedContacts(blockedContacts)
    }

    override suspend fun getBlockedContacts(): List<BlockedContact> {
        return blockedContactDao.getBlockedContacts()
    }

    override suspend fun clearTable() {
        blockedContactDao.clearTable()
    }
}