package com.naposystems.pepito.entity.conversation

import androidx.room.Embedded
import androidx.room.Relation
import com.naposystems.pepito.entity.Contact

data class ConversationAndContact(
    @Embedded var contact: Contact,
    @Relation(
        entityColumn = "contact_id",
        parentColumn = "id",
        entity = Conversation::class
    )
    val conversation: Conversation?
)