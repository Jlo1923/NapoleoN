package com.naposystems.pepito.dto.contacts

import com.naposystems.pepito.entity.Contact
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactsResDTO(
    @Json(name = "id") val int: Int,
    @Json(name = "nick") val nickname: String,
    @Json(name = "names") val displayName: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatar: String
) {
    companion object {

        fun toEntityList(contactsResDTO: List<ContactsResDTO>): List<Contact> {
            val listContacts: MutableList<Contact> = arrayListOf()

            for (resContact in contactsResDTO) {

                val contact = Contact(
                    resContact.avatar,
                    resContact.nickname,
                    resContact.displayName,
                    resContact.status,
                    resContact.lastSeen
                )

                listContacts.add(contact)
            }

            return listContacts
        }
    }
}