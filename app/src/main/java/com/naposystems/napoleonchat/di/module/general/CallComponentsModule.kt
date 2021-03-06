package com.naposystems.napoleonchat.di.module.general

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.naposystems.napoleonchat.BuildConfig
import dagger.Module
import dagger.Provides
import org.webrtc.*
import javax.inject.Singleton


@Module
class CallComponentsModule {
//
//    @Provides
//    fun provideEglBase(): EglBase {
//        return EglBase.create()
//    }
//
//    @Provides
//    fun providePeerConnectionFactory(context: Context, eglBase: EglBase): PeerConnectionFactory {
//        //Initialize PeerConnectionFactory globals.
//        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
//            .createInitializationOptions()
//        PeerConnectionFactory.initialize(initializationOptions)
//
//        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
//        val options = PeerConnectionFactory.Options()
//        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
//            eglBase.eglBaseContext,
//            /* enableIntelVp8Encoder */true,
//            /* enableH264HighProfile */true
//        )
//        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
//
//        return PeerConnectionFactory.builder()
//            .setOptions(options)
//            .setVideoEncoderFactory(defaultVideoEncoderFactory)
//            .setVideoDecoderFactory(defaultVideoDecoderFactory)
//            .createPeerConnectionFactory()
//    }
//
//    @Provides
//    fun providePeerConnectionIceServer(): ArrayList<PeerConnection.IceServer> {
//        return arrayListOf(
//            PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER)
//                .createIceServer(),
//
//            PeerConnection.IceServer.builder(BuildConfig.TURN_SERVER)
//                .setUsername("wPJlHAYY")
//                .setPassword("GrI09zxkwFuOihIf")
//                .createIceServer()
//        )
//    }
//
//    @Provides
//    fun provideRTCConfiguration(peerIceServer: ArrayList<PeerConnection.IceServer>): PeerConnection.RTCConfiguration {
//
//        val rtcConfiguration = PeerConnection.RTCConfiguration(peerIceServer)
//
//        rtcConfiguration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
//
//        rtcConfiguration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
//
//        rtcConfiguration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
//
//        rtcConfiguration.continualGatheringPolicy =
//            PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
//
//        rtcConfiguration.keyType = PeerConnection.KeyType.ECDSA
//
//        return rtcConfiguration
//
//    }

}