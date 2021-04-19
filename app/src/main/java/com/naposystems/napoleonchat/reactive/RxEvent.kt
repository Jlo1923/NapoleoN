package com.naposystems.napoleonchat.reactive

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class RxEvent {
    data class NewMessageEventForCounter(val contactId: Int)
    class NewFriendshipRequest
    class CancelOrRejectFriendshipRequestEvent
    class FriendshipRequestAccepted
    class NoInternetConnection
    class AccountAttack
    class HideOptionMenuRecoveryAccount
    data class IncomingCall(val callModel: CallModel)
    class CallEnd
    data class EmojiSelected(val emoji: Emoji)
    data class HeadsetState(val state: Int)
    data class MessagesToEliminate(val id: List<MessageAttachmentRelation>)
    data class EnableButtonPlayAudio(val state: Boolean)
    data class ContactBlockOrDelete(val contactId: Int)
    data class DeleteChannel(val contact: ContactEntity)
    data class HangupByNotification(val channel: String)
    class CreateNotification
    class IncomingCallSystem

    data class UploadStart(val attachmentEntity: AttachmentEntity)
    data class UploadSuccess(val attachmentEntity: AttachmentEntity)
    data class UploadError(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    )

    data class CompressProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class UploadProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class MultiUploadStart(val attachmentEntity: AttachmentEntity)

    class MultiUploadTryNextAttachment()

    data class MultiUploadSuccess(val attachmentEntity: AttachmentEntity)

    data class MultiUploadError(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    )

    data class MultiCompressProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class MultiUploadProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class StateFlag(val state: Int)
}