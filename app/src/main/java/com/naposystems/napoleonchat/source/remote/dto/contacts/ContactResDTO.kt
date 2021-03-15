package com.naposystems.napoleonchat.source.remote.dto.contacts

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import timber.log.Timber

@JsonClass(generateAdapter = true)
data class ContactResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "nick") val nickname: String,
    @Json(name = "fullname") val displayName: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatar: String,
    @Json(name = "silence") val silence: Boolean = false,
    @Json(name = "isFriend") val isFriend: Boolean?,
    @Json(name = "isBlock") val isBlock: Boolean?,
    @Json(name = "request_receiver") val receiver: ContactFriendshipResDTO?,
    @Json(name = "request_offer") val offer: ContactFriendshipResDTO?
) {
    companion object {

        fun toEntityList(
            contactResDTO: List<ContactResDTO>,
            statusBlocked: Boolean = false
        ): List<ContactEntity> {
            val listContacts: MutableList<ContactEntity> = arrayListOf()

            for (resContact in contactResDTO) {
                val contact = toEntity(resContact, statusBlocked)
                listContacts.add(contact)
            }

            return listContacts
        }

        fun toEntity(response: ContactResDTO, statusBlocked: Boolean = false): ContactEntity {

            var isBlocked = statusBlocked
            if (!isBlocked) isBlocked = response.isBlock ?: false

            return ContactEntity(
                response.id,
                imageUrl = response.avatar,
                nickname = response.nickname,
                displayName = response.displayName,
                status = response.status,
                lastSeen = response.lastSeen,
                statusBlocked = isBlocked,
                statusFriend = response.isFriend ?: false,
                receiver = response.receiver != null,
                offer = response.offer != null,
                offerId = response.offer?.id
            )
        }
    }
}