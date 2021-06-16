package com.naposystems.napoleonchat.ui.multi.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultipleAttachmentItemAttachment(
    val fileName: String,
    val status: Int,
    val webId: String,
    val extension: String = "",
    val type: String,
    var body: String,
    var totalSelfDestructionAt: Long,
    var thumbnailUri: Uri
) : Parcelable {
    fun toAttachmentEntity(): AttachmentEntity = AttachmentEntity(
        id = 0,
        messageId = 0,
        webId = this.webId,
        messageWebId = "",
        type = this.type,
        body = "",
        fileName = this.fileName,
        origin = 0,
        thumbnailUri = "",
        status = this.status,
        extension = this.extension,
        duration = 0,
        isCompressed = false,
        selfDestructionAt = 0,
        totalSelfDestructionAt = 0,
        updatedAt = 0
    )

    fun isReaded() = status == Constants.AttachmentStatus.READED.status
}

