package com.naposystems.napoleonchat.webRTC

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection.*
import org.webrtc.RtpReceiver
import timber.log.Timber

open class CustomPeerConnectionObserver : Observer {

    override fun onSignalingChange(signalingState: SignalingState) {
        Timber.d("LLAMADA PASO: onSignalingChange() called with: signalingState = [$signalingState]")
    }

    override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
        Timber.d("LLAMADA PASO: onIceConnectionChange() called with: iceConnectionState = [$iceConnectionState]")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Timber.d("LLAMADA PASO: onIceConnectionReceivingChange() called with: b = [$b]")
    }

    override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {
        Timber.d("LLAMADA PASO: onIceGatheringChange() called with: iceGatheringState = [$iceGatheringState]")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Timber.d("LLAMADA PASO: onIceCandidate() called with: iceCandidate = [$iceCandidate]")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate?>) {
        Timber.d("LLAMADA PASO: onIceCandidatesRemoved() called with: iceCandidates = [$iceCandidates]")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Timber.d("LLAMADA PASO: onAddStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Timber.d("LLAMADA PASO: onRemoveStream() called with: mediaStream = [$mediaStream]")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Timber.d("LLAMADA PASO: onDataChannel() called with: dataChannel = [$dataChannel]")
    }

    override fun onRenegotiationNeeded() {
        Timber.d("LLAMADA PASO: onRenegotiationNeeded() called")
    }

    override fun onAddTrack(
        rtpReceiver: RtpReceiver,
        mediaStreams: Array<MediaStream>
    ) {
        Timber.d("LLAMADA PASO: onAddTrack() called with: rtpReceiver = [$rtpReceiver], mediaStreams = [$mediaStreams]")
    }
}