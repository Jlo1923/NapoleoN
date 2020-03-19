package com.naposystems.pepito.model.emojiKeyboard

import androidx.recyclerview.widget.DiffUtil
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Emoji(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "code") val code: Int
) {
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
}