package com.naposystems.napoleonchat.model.typeSubscription

data class TypeSubscription (
    val id: Int,
    val description: String,
    val type: Int,
    val quantity: Int,
    val price: Double
) {
    override fun toString(): String {
        return description
    }
}