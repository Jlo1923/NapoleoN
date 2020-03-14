package com.naposystems.pepito.entity.message

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
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
    var attachmentList: List<Attachment>
) : Parcelable