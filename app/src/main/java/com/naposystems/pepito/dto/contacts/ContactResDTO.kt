package com.naposystems.pepito.dto.contacts

import com.naposystems.pepito.dto.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.entity.BlockedContact
import com.naposystems.pepito.entity.Contact
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "nick") val nickname: String,
    @Json(name = "fullname") val displayName: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatar: String
) {
    companion object {

        fun toEntityList(contactResDTO: List<ContactResDTO>): List<Contact> {
            val listContacts: MutableList<Contact> = arrayListOf()

            for (resContact in contactResDTO) {

                val contact = toEntity(resContact)

                listContacts.add(contact)
            }

            return listContacts
        }

        fun toEntity(response: ContactResDTO): Contact {
            return Contact(
                response.id,
                response.avatar,
                response.nickname,
                response.displayName,
                response.status,
                response.lastSeen
            )
        }

        fun toBlockedContactEntityList(blockedContacts: List<ContactResDTO>): List<BlockedContact> {
            val listBlockedContacts: MutableList<BlockedContact> = arrayListOf()

            for (blockedContact in blockedContacts) {
                val entity = BlockedContact(
                    blockedContact.id,
                    blockedContact.avatar,
                    blockedContact.nickname,
                    blockedContact.displayName,
                    blockedContact.status,
                    blockedContact.lastSeen
                )

                listBlockedContacts.add(entity)
            }

            return listBlockedContacts
        }
    }
}