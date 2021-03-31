package com.naposystems.napoleonchat.model.addContact

data class Contact(
    val id: Int,
    val imageUrl: String,
    val nickname: String,
    val displayName: String,
    var status: String,
    var lastSeen: String,
    var statusFriend: Boolean,
    var statusBlocked: Boolean,
    var receiver: Boolean?,
    var offer: Boolean?,
    val offerId: Int?,
)