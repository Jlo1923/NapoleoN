package com.naposystems.napoleonchat.source.remote.dto.contacts

import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Constants
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
    @Json(name = "silence") val silence: Boolean = false,
    @Json(name = "isFriend") val isFriend: Boolean?,
    @Json(name = "isBlock") val isBlock: Boolean?,
    @Json(name = "request_receiver") val receiver: ContactFriendshipResDTO?,
    @Json(name = "request_offer") val offer: ContactFriendshipResDTO?,
    @Json(name = "full_name_fake") val fullNameFake: String?,
    @Json(name = "nick_fake") val nickNameFake: String?,
    @Json(name = "avatar_fake") val avatarFake: String?,

    ) {
    companion object {

        fun getUsers(contactResDTO: List<ContactResDTO>): MutableList<Contact> {

            val listContacts: MutableList<Contact> = arrayListOf()

            for (resContact in contactResDTO) {
                val contact = Contact(
                    id = resContact.id,
                    imageUrl = resContact.avatar,
                    nickname = resContact.nickname,
                    displayName = resContact.displayName,
                    status = resContact.status,
                    lastSeen = resContact.lastSeen,
                    statusFriend = resContact.isFriend ?: false,
                    statusBlocked = resContact.isBlock ?: false,
                    receiver = resContact.receiver != null,
                    offer = resContact.offer != null,
                    offerId = resContact.offer?.id
                )

                listContacts.add(contact)
            }

            val multableList: MutableList<Contact> = mutableListOf()
            val sortedByFriends = listContacts.sortedByDescending { it.statusFriend }
            val existsContact = sortedByFriends.findLast { it.statusFriend }

            val title1 =
                Contact(id = -1, type = Constants.AddContactTitleType.TITLE_MY_CONTACTS.type)
            val title2 = Contact(
                id = -2,
                type = Constants.AddContactTitleType.TITLE_COINCIDENCES.type
            )

            if (existsContact != null) {
                multableList.add(title1)
                multableList.addAll(sortedByFriends)
                val lastP = multableList.indexOf(existsContact)
                    multableList.add(
                        lastP + 1,
                        title2
                    )
            } else {
                if (sortedByFriends.isNotEmpty())
                    multableList.add(title2)
                multableList.addAll(sortedByFriends)
            }
            return multableList
        }


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

            return ContactEntity(
                response.id,
                imageUrl = response.avatar,
                nickname = response.nickname,
                displayName = response.displayName,
                status = response.status,
                lastSeen = response.lastSeen,
                statusBlocked = statusBlocked,
                imageUrlFake = if (response.avatarFake.isNullOrEmpty()) response.avatar else response.avatarFake,
                displayNameFake = if (response.fullNameFake.isNullOrEmpty()) response.displayName else response.fullNameFake,
                nicknameFake = if (response.nickNameFake.isNullOrEmpty()) response.nickname else response.nickNameFake

            )
        }

        fun toEntity(contactModel: Contact): ContactEntity {
            return ContactEntity(
                id = contactModel.id,
                imageUrl = contactModel.imageUrl,
                nickname = contactModel.nickname,
                displayName = contactModel.displayName,
                status = contactModel.status,
                lastSeen = contactModel.lastSeen,
                statusBlocked = false,
                imageUrlFake = contactModel.imageUrl,
                displayNameFake = contactModel.displayName,
                nicknameFake = contactModel.nickname

            )
        }
    }

}