package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact ORDER BY display_name ASC")
    fun getContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact ORDER BY display_name ASC")
    suspend fun getLocalContacts(): List<Contact>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Delete
    suspend fun deleteContacts(contacts: List<Contact>)
}