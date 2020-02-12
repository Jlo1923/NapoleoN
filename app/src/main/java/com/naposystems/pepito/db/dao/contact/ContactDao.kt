package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact WHERE status_blocked = 0 ORDER BY display_name ASC")
    fun getContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY display_name ASC")
    suspend fun getLocalContacts(): List<Contact>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Query("SELECT * FROM contact WHERE status_blocked = 1 ORDER BY display_name ASC")
    fun getBlockedContacts(): LiveData<List<Contact>>

    @Query("UPDATE contact SET status_blocked = 1 WHERE id=:contactId")
    suspend fun blockContact(contactId: Int)

    @Query("UPDATE contact SET status_blocked = 0 WHERE id=:contactId")
    suspend fun unblockContact(contactId: Int)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Delete
    suspend fun deleteContacts(contacts: List<Contact>)

}