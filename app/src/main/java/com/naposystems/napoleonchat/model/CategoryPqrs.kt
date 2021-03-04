package com.naposystems.napoleonchat.model

data class CategoryPqrs(
    val id: Int,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}