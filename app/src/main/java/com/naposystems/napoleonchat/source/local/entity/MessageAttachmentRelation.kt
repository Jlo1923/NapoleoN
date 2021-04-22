package com.naposystems.napoleonchat.source.local.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Relation
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageAttachmentRelation(
    @Embedded
    var messageEntity: MessageEntity,
    @Relation(
        entity = AttachmentEntity::class,
        entityColumn = DBConstants.Attachment.COLUMN_MESSAGE_ID,
        parentColumn = DBConstants.Message.COLUMN_ID
    )
    var attachmentEntityList: List<AttachmentEntity>,
    @Relation(
        entity = QuoteEntity::class,
        entityColumn = DBConstants.Quote.COLUMN_MESSAGE_ID,
        parentColumn = DBConstants.Message.COLUMN_ID
    )
    @Nullable
    var quoteEntity: QuoteEntity?,
    @Relation(
        entity = ContactEntity::class,
        entityColumn = DBConstants.Contact.COLUMN_ID,
        parentColumn = DBConstants.Message.COLUMN_CONTACT_ID
    )
    @Nullable
    var contact: ContactEntity?,
    @Nullable
    var messagesUnReads: Int? = 0
) : Parcelable {

    fun getFirstAttachment(): AttachmentEntity? {
        var firstAttachmentEntity: AttachmentEntity? = null

        if (attachmentEntityList.isNotEmpty()) {
            firstAttachmentEntity = attachmentEntityList.first()
        }

        return firstAttachmentEntity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageAttachmentRelation

        if (messageEntity != other.messageEntity) return false
        if (attachmentEntityList != other.attachmentEntityList) return false
        if (quoteEntity != other.quoteEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageEntity.hashCode()
        result = 31 * result + attachmentEntityList.hashCode()
        result = 31 * result + (quoteEntity?.hashCode() ?: 0)
        return result
    }

    fun isMine(): Boolean {
        return messageEntity.isMine == Constants.IsMine.YES.value
    }
}