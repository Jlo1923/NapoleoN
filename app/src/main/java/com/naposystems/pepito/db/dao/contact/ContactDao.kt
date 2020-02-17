package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact WHERE status_blocked = 0 ORDER BY display_name ASC")
    fun getContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact WHERE status_blocked = 0 ORDER BY display_name ASC")
    suspend fun getLocalContacts(): List<Contact>

    @Query("SELECT * FROM contact WHERE id=:id")
    suspend fun getContactById(id: Int): List<Contact>

    @Query("SELECT * FROM contact WHERE id = :idContact")
    fun getContact(idContact: Int): LiveData<Contact>

    @Query("UPDATE contact SET silenced = :contactSilenced WHERE id = :idContact")
    fun updateContactSilenced(idContact: Int, contactSilenced : Int)

    @Query("UPDATE contact SET display_name_fake = :nameFake WHERE id = :idContact ")
    suspend fun updateNameFakeLocalContact(idContact: Int, nameFake: String)

    @Query("UPDATE contact SET image_url_fake = :avatarFake WHERE id = :idContact ")
    suspend fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String)

    @Query("UPDATE contact SET nickname_fake = :nickNameFake WHERE id = :idContact ")
    suspend fun updateNickNameFakeContact(idContact: Int, nickNameFake: String)

    @Query("UPDATE contact SET image_url_fake = :avatarFake WHERE id = :idContact ")
    suspend fun updateAvatarFakeContact(idContact: Int, avatarFake: String)

    @Query("UPDATE contact SET display_name_fake = '', nickname_fake = '', image_url_fake = ''  WHERE id = :idContact ")
    suspend fun restoreLocalContact(idContact: Int)

    @Query("DELETE FROM message WHERE user_addressee = :idContact")
    suspend fun deleteMessages(idContact: Int)

    @Query("UPDATE conversation SET message = '', created = 0, unreads = 0 WHERE contact_id = :idContact")
    suspend fun cleanConversation(idContact: Int)

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
}