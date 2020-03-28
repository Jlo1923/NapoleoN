package com.naposystems.pepito.model.emojiKeyboard

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

open class EmojiCategory : Serializable {
    var id: Int? = null
    var name: String? = null
    var emojiList: ArrayList<Emoji>? = null
}