package com.naposystems.napoleonchat.reactive

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class RxEvent {
    data class NewMessageReceivedEvent(val channelName: String, val jsonObject: JSONObject)
    data class NewMessageEventForCounter(val contactId: Int)
    class NewFriendshipRequest
    class CancelOrRejectFriendshipRequestEvent
    class FriendshipRequestAccepted
    class NoInternetConnection
    class AccountAttack
    class HideOptionMenuRecoveryAccount

    //    data class ItsSubscribedToCallChannel(val channel: String, val contactId: Int, val isVideoCall: Boolean)
    data class IncomingCall(val callModel: CallModel)

    //    data class IceCandidateReceived(val channel: String, val iceCandidate: IceCandidate)
//    data class OfferReceived(val channel: String, val sessionDescription: SessionDescription)
//    data class AnswerReceived(val channel: String, val sessionDescription: SessionDescription)
//    data class ContactHasHangup(val channel: String)
//    data class ContactWantChangeToVideoCall(val channel: String)
//    data class ContactAcceptChangeToVideoCall(val channel: String)
//    data class ContactCancelChangeToVideoCall(val channel: String)
//    data class ContactTurnOffCamera(val channel: String)
//    data class ContactTurnOnCamera(val channel: String)
//    data class ContactRejectCall(val channel: String)
    class CallEnd
    data class EmojiSelected(val emoji: Emoji)
    data class HeadsetState(val state: Int)
    data class MessagesToEliminate(val id: List<MessageAttachmentRelation>)
    data class EnableButtonPlayAudio(val state: Boolean)

    //    data class ContactCancelCall(val channel: String)
    data class ContactBlockOrDelete(val contactId: Int)
    data class DeleteChannel(val contact: ContactEntity)
    class HangupByNotification

    //    data class ContactCantChangeToVideoCall(val channel: String)
    data class RejectCallByNotification(val channel: String)
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

    class MultiUploadTryNextAttachment

    data class MultiUploadSuccess(val attachmentEntity: AttachmentEntity)

    data class MultiUploadError(
        val messageEntity: MessageEntity,
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    )

    class ExitOfService

    data class MultiCompressProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class MultiUploadProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class MultiDownloadStart(val attachmentEntity: AttachmentEntity)

    class MultiDownloadTryNextAttachment

    data class MultiDownloadSuccess(val attachmentEntity: AttachmentEntity)

    data class MultiDownloadError(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    )

    data class MultiDownloadProgress(
        val attachmentEntity: AttachmentEntity,
        val progress: Float
    )

    data class StateFlag(val state: Int)
    class ConnectSocket {

    }
    data class SubscriptionStatusEvent(val status: SubscriptionStatus)
}