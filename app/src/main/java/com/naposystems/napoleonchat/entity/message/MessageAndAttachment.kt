package com.naposystems.napoleonchat.entity.message

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Relation
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
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
    var contact: Contact,
    @Nullable
    val messagesUnReads: Int? = 0
) : Parcelable {

    fun getFirstAttachment(): Attachment? {
        var firstAttachment: Attachment? = null

        if (attachmentList.isNotEmpty()) {
            firstAttachment = attachmentList.first()
        }

        return firstAttachment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageAndAttachment

        if (message != other.message) return false
        if (attachmentList != other.attachmentList) return false
        if (quote != other.quote) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + attachmentList.hashCode()
        result = 31 * result + (quote?.hashCode() ?: 0)
        return result
    }

    /*override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageAndAttachment

        if (message != other.message) return false
        if (attachmentList != other.attachmentList) return false

        return true
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }*/


}