package com.naposystems.napoleonchat.entity

data class CategoryPqrs(
    val id: Int,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}