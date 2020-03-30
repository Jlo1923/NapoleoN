package com.naposystems.pepito.entity.message

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Relation
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.attachments.Attachment
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageAndAttachment(
    @Embedded
    var message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id",
        entity = Attachment::class
    )
    var attachmentList: List<Attachment>,
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id",
        entity = Quote::class
    )
    @Nullable
    var quote: Quote?,
    @Relation(
        parentColumn = "contact_id",
        entityColumn = "id",
        entity = Contact::class
    )
    var contact: Contact
) : Parcelable {
    companion object {

        fun getFirstAttachment(messageAndAttachment: MessageAndAttachment): Attachment? {
            var firstAttachment: Attachment? = null

            if (messageAndAttachment.attachmentList.isNotEmpty()) {
                firstAttachment = messageAndAttachment.attachmentList.first()
            }

            return firstAttachment
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageAndAttachment

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }


}