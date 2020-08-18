package com.naposystems.napoleonchat.model.emojiKeyboard

import androidx.recyclerview.widget.DiffUtil
import java.io.Serializable

class Emoji(val id: Int, val name: String, vararg val code: Int): Serializable {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Emoji>() {
            override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji) =
                oldItem == newItem
        }
    }

    override fun toString(): String {
        return "ID: $id, Name: $name, Code: $code"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Emoji

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}