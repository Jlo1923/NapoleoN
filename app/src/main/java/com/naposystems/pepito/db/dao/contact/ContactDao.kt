package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact WHERE status_blocked = 0 ORDER BY display_name ASC")
    fun getContacts(): LiveData<MutableList<Contact>>

    @Query("SELECT * FROM contact WHERE status_blocked = 0 ORDER BY display_name ASC")
    suspend fun getLocalContacts(): List<Contact>

    @Query("SELECT * FROM contact WHERE id=:contactId")
    fun getContactById(contactId: Int): Contact?

    @Query("SELECT * FROM contact WHERE id = :contactId")
    fun getContact(contactId: Int): LiveData<Contact>

    @Query("UPDATE contact SET silenced = :contactSilenced WHERE id = :contactId")
    fun updateContactSilenced(contactId: Int, contactSilenced : Int)

    @Query("UPDATE contact SET display_name_fake = :nameFake WHERE id = :contactId ")
    suspend fun updateNameFakeContact(contactId: Int, nameFake: String)

    @Query("UPDATE contact SET image_url_fake = :avatarFake WHERE id = :contactId ")
    suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)

    @Query("UPDATE contact SET nickname_fake = :nickNameFake WHERE id = :contactId ")
    suspend fun updateNickNameFakeContact(contactId: Int, nickNameFake: String)

    @Query("UPDATE contact SET display_name_fake = '', nickname_fake = '', image_url_fake = ''  WHERE id = :contactId ")
    suspend fun restoreContact(contactId: Int)

    @Query("UPDATE contact SET image_url = '', image_url_fake = '' WHERE id = :contactId ")
    suspend fun restoreImageByContact(contactId: Int)

    @Insert
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Update
    suspend fun updateContact(contact: Contact)

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

    @Query("UPDATE contact SET self_destruct_time=:selfDestructTime WHERE id=:contactId ")
    suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

    @Query("SELECT self_destruct_time FROM contact WHERE id=:contactId ")
    fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int>
}