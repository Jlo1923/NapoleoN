package com.naposystems.napoleonchat.model.addContact


data class Contact(
    val id: Int,
    val imageUrl: String = "",
    val nickname: String = "",
    val displayName: String = "",
    var status: String = "",
    var lastSeen: String = "",
    var statusFriend: Boolean = false,
    var statusBlocked: Boolean = false,
    var receiver: Boolean = false,
    var offer: Boolean = false,
    val offerId: Int? = 0,
    val type: Int = 0

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (id != other.id) return false
        if (nickname != other.nickname) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }


}