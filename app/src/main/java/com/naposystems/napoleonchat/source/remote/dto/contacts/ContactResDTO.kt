package com.naposystems.napoleonchat.source.remote.dto.contacts

import com.naposystems.napoleonchat.model.addContact.AddContactTitle
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
    @Json(name = "request_offer") val offer: ContactFriendshipResDTO?
) {
    companion object {

        fun getUsers(contactResDTO: List<ContactResDTO>): MutableList<Any> {

            val listContacts: MutableList<Contact> = arrayListOf()
            var existsC = false

            for (resContact in contactResDTO) {
                val contact = Contact(
                    resContact.id,
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

                if (!existsC && !contact.statusFriend) existsC = true
                listContacts.add(contact)
            }

            val multableList: MutableList<Any> = mutableListOf()
            val sortedByFriends = listContacts.sortedByDescending { o -> o.statusFriend }
            val existsContact = sortedByFriends.findLast { it.statusFriend }

            val title1 = AddContactTitle(1, Constants.AddContactTitleType.TITLE_MY_CONTACTS.type)
            val title2 = AddContactTitle(2, Constants.AddContactTitleType.TITLE_COINCIDENCES.type)

            if (existsContact != null) {
                multableList.add(title1)
                multableList.addAll(sortedByFriends)
                val lastP = multableList.indexOf(existsContact)
                if (existsC)
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
                statusBlocked = statusBlocked
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
                statusBlocked = false
            )
        }
    }

}