package com.naposystems.napoleonchat.entity.addContact

open class FriendShipRequestAdapterType(
    val uid: Int,
    var type: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendShipRequestAdapterType

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid
    }
}