package com.naposystems.napoleonchat.webRTC

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnection.SignalingState
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.PeerConnection.IceGatheringState
import org.webrtc.RtpReceiver
import timber.log.Timber

open class CustomPeerConnectionObserver : Observer {

    override fun onSignalingChange(signalingState: SignalingState) {
        Timber.d("onSignalingChange() called with: signalingState = [$signalingState]")
    }

    override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
        Timber.d("onIceConnectionChange() called with: iceConnectionState = [$iceConnectionState]")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Timber.d("onIceConnectionReceivingChange() called with: b = [$b]")
    }

    override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {
        Timber.d("onIceGatheringChange() called with: iceGatheringState = [$iceGatheringState]")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Timber.d("onIceCandidate() called with: iceCandidate = [$iceCandidate]")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate?>) {
        Timber.d("onIceCandidatesRemoved() called with: iceCandidates = [$iceCandidates]")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Timber.d("onAddStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Timber.d("onRemoveStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Timber.d("onDataChannel() called with: dataChannel = [$dataChannel]")
    }

    override fun onRenegotiationNeeded() {
        Timber.d("onRenegotiationNeeded() called")
    }

    override fun onAddTrack(
        rtpReceiver: RtpReceiver,
        mediaStreams: Array<MediaStream>
    ) {
        Timber.d("onAddTrack() called with: rtpReceiver = [$rtpReceiver], mediaStreams = [$mediaStreams]")
    }
}