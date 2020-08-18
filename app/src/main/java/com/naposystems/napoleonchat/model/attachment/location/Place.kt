package com.naposystems.napoleonchat.model.attachment.location

import androidx.recyclerview.widget.DiffUtil

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val distanceInMeters: Int
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(
                oldItem: Place,
                newItem: Place
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Place,
                newItem: Place
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}