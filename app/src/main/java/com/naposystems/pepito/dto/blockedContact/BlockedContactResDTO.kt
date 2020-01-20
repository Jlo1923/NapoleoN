package com.naposystems.pepito.dto.blockedContact

import com.naposystems.pepito.entity.BlockedContact
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockedContactResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "nick") val nickname: String,
    @Json(name = "names") val displayName: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatar: String
) {
    companion object {

        fun toEntityList(blockedContacts: List<BlockedContactResDTO>): List<BlockedContact> {
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