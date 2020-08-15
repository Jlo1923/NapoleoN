package com.naposystems.napoleonchat.dto.contacts

import com.naposystems.napoleonchat.entity.Contact
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "nick") val nickname: String,
    @Json(name = "fullname") val displayName: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatar: String,
    @Json(name = "silence") val silence: Boolean = false
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
                imageUrl = response.avatar,
                nickname = response.nickname,
                displayName = response.displayName,
                status = response.status,
                lastSeen = response.lastSeen
            )
        }
    }
}