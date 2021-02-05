package com.naposystems.napoleonchat.reactive

import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
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
    data class IncomingCall(val channel: String, val contactId: Int, val isVideoCall: Boolean)
    data class ContactHasJoinToCall(val channel: String)
    data class IceCandidateReceived(val channel: String, val iceCandidate: IceCandidate)
    data class OfferReceived(val channel: String, val sessionDescription: SessionDescription)
    data class AnswerReceived(val channel: String, val sessionDescription: SessionDescription)
    data class ContactHasHangup(val channel: String)
    data class ContactWantChangeToVideoCall(val channel: String)
    data class ContactAcceptChangeToVideoCall(val channel: String)
    data class ContactCancelChangeToVideoCall(val channel: String)
    data class ContactTurnOffCamera(val channel: String)
    data class ContactTurnOnCamera(val channel: String)
    data class ContactRejectCall(val channel: String)
    class CallEnd
    data class EmojiSelected(val emoji: Emoji)
    data class HeadsetState(val state: Int)
    data class MessagesToEliminate(val id: List<MessageAndAttachment>)
    data class EnableButtonPlayAudio(val state: Boolean)
    data class ContactCancelCall(val channel: String)
    data class ContactBlockOrDelete(val contactId: Int)
    data class DeleteChannel(val contact: Contact)
    data class HangupByNotification(val channel: String)
    data class ContactCantChangeToVideoCall(val channel: String)
    data class RejectCallByNotification(val channel: String)
    class CreateNotification
    class IncomingCallSystem

    data class UploadStart(val attachment: Attachment)
    data class UploadSuccess(val attachment: Attachment)
    data class UploadError(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    )
    data class CompressProgress(
        val attachment: Attachment,
        val progress: Float
    )
    data class UploadProgress(
        val attachment: Attachment,
        val progress: Float
    )

    data class StateFlag(val state: Int)
}