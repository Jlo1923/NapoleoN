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
class CallModule {

    @Provides
    @Singleton
    fun provideEglBase(): EglBase {
        return EglBase.create()
    }

    @Provides
    @Singleton
    fun providePeerConnectionFactory(context: Context, eglBase: EglBase): PeerConnectionFactory {
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            /* enableIntelVp8Encoder */true,
            /* enableH264HighProfile */true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    @Provides
    @Singleton
    fun providePeerConnectionIceServer(): ArrayList<PeerConnection.IceServer> {
        return arrayListOf(
            PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER)
                .createIceServer(),
            PeerConnection.IceServer.builder(BuildConfig.TURN_SERVER)
                .setUsername("wPJlHAYY")
                .setPassword("GrI09zxkwFuOihIf")
                .createIceServer()
        )
    }

    @Provides
    @Singleton
    fun provideMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                    .build()
            )
        }
    }

}