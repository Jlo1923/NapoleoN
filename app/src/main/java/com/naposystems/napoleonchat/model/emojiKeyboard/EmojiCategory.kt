package com.naposystems.napoleonchat.model.emojiKeyboard

import java.io.Serializable

open class EmojiCategory : Serializable {
    var id: Int? = null
    var name: String? = null
    var emojiList: ArrayList<Emoji>? = null
}