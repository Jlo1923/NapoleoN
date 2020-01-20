package com.naposystems.pepito.db.dao.blockedContacts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.pepito.entity.BlockedContact

@Dao
interface BlockedContactDao {

    @Insert
    fun insertBlockedContacts(blockedContacts: List<BlockedContact>)

    @Query("SELECT * FROM blocked_contacts")
    suspend fun getBlockedContacts(): List<BlockedContact>

    @Query("DELETE FROM blocked_contacts")
    suspend fun clearTable()
}