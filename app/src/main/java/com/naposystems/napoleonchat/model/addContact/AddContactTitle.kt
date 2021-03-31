package com.naposystems.napoleonchat.model.addContact

class AddContactTitle(
    val id: Int,
    var type: Int
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddContactTitle

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }


}