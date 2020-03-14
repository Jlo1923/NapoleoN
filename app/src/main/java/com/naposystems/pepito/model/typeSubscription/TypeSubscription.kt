package com.naposystems.pepito.model.typeSubscription

data class TypeSubscription (
    val id: Int,
    val description: String,
    val type: Int,
    val quantity: Int,
    val price: Int
) {
    override fun toString(): String {
        return description
    }
}