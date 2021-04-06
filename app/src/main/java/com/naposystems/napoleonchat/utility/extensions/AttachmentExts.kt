package com.naposystems.napoleonchat.utility.extensions

import android.os.Message
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

fun MultipleAttachmentFileItem.isVideo(): Boolean {
    return this.attachmentType == Constants.AttachmentType.VIDEO.type
}

fun AttachmentEntity.getSelfAutoDestructionForSave(selfDestructTime: Int): Int {
    val durationAttachment = TimeUnit.MILLISECONDS.toSeconds(duration).toInt()
    return Utils.compareDurationAttachmentWithSelfAutoDestructionInSeconds(
        durationAttachment, selfDestructTime
    )
}

fun ItemMessage.getMessageEntityForCreate(): MessageEntity {
    return MessageEntity(
        id = 0,
        webId = "",
        uuid = UUID.randomUUID().toString(),
        body = messageString,
        quoted = quote,
        contactId = contact?.id ?: 0,
        updatedAt = 0,
        createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
        isMine = Constants.IsMine.YES.value,
        status = Constants.MessageStatus.SENDING.status,
        numberAttachments = numberAttachments,
        messageType = Constants.MessageType.MESSAGE.type,
        selfDestructionAt = selfDestructTime
    )
}

fun File.toAttachmentEntityAudio(mediaStoreAudio: MediaStoreAudio): AttachmentEntity =
    AttachmentEntity(
        id = 0,
        messageId = 0,
        webId = "",
        messageWebId = "",
        type = Constants.AttachmentType.AUDIO.type,
        body = "",
        fileName = name,
        origin = Constants.AttachmentOrigin.AUDIO_SELECTION.origin,
        thumbnailUri = "",
        status = Constants.AttachmentStatus.SENDING.status,
        extension = "mp3",
        duration = mediaStoreAudio.duration
    )

fun File.toAttachmentEntityDocument(): AttachmentEntity =
    AttachmentEntity(
        id = 0,
        messageId = 0,
        webId = "",
        messageWebId = "",
        type = Constants.AttachmentType.DOCUMENT.type,
        body = "",
        fileName = name,
        origin = Constants.AttachmentOrigin.GALLERY.origin,
        thumbnailUri = "",
        status = Constants.AttachmentStatus.SENDING.status,
        extension = extension
    )

fun MultipleAttachmentFileItem.toAttachmentEntityWithFile(file: File): AttachmentEntity =
    AttachmentEntity(
        id = 0,
        messageId = 0,
        webId = "",
        messageWebId = "",
        type = this.attachmentType,
        body = "",
        fileName = file.name,
        origin = Constants.AttachmentOrigin.GALLERY.origin,
        thumbnailUri = "",
        status = Constants.AttachmentStatus.SENDING.status,
        duration = 0L,
        extension = this.getExtensionByType()
    )

fun MessageEntity.toMessageReqDto(cryptoMessage: CryptoMessage): MessageReqDTO = MessageReqDTO(
    userDestination = contactId,
    quoted = "",
    body = getBody(cryptoMessage),
    numberAttachments = numberAttachments,
    destroy = selfDestructionAt,
    messageType = Constants.MessageType.MESSAGE.type,
    uuidSender = uuid
)

private fun MultipleAttachmentFileItem.getExtensionByType() =
    if (isVideo()) {
        "mp4"
    } else {
        "jpg"
    }
