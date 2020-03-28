package com.naposystems.pepito.utility.emojiManager

import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.utility.emojiManager.categories.*

class EmojiManager private constructor() {

    private object HOLDER {
        val INSTANCE = EmojiManager()
    }

    companion object {
        val instance: EmojiManager by lazy { HOLDER.INSTANCE }
    }

    private val mListCategories = mutableListOf<EmojiCategory>()

    fun getEmojiCategories() = mListCategories

    fun install(){
        mListCategories.add(SmileysAndPeopleCategory())
        mListCategories.add(AnimalsAndNatureCategory())
        mListCategories.add(FoodAndDrinkCategory())
        mListCategories.add(ActivityCategory())
        mListCategories.add(TravelAndPlacesCategory())
        mListCategories.add(ObjectsCategory())
        mListCategories.add(SymbolsCategory())
        mListCategories.add(FlagsCategory())
    }
}
