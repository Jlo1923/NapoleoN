package com.naposystems.napoleonchat.source.remote.dto.contacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactsResDTO(
    @Json(name = "friends") var contacts: List<ContactResDTO> = ArrayList(),
    @Json(name = "date") val date: Long
)