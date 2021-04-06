package com.naposystems.napoleonchat.utility.mediaPlayer

import android.content.Context
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar

interface IContractMediaPlayer {
    fun setMessageId(messageId: Int)
    fun setAudioUri(uri: Uri?)
    fun setAudioFileName(fileName: String)
    fun playAudio(progress: Int = 0, isEarpiece: Boolean = false)
    fun pauseAudio()
    fun registerProximityListener()
    fun unregisterProximityListener()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setImageButtonPlay(imageButtonPlay: ImageView)
    fun setImageButtonSpeed(imageButtonSpeed: ImageButton)
    fun setStateImageButtonSpeed(imageButtonSpeed: ImageButton, messageId: Int)
    fun setSeekbar(seekBar: AppCompatSeekBar)
    fun setTextViewDuration(textView: TextView)
    fun setListener(listener: MediaPlayerManager.Listener)
    fun rewindMilliseconds(messageId: Int, millis: Long)
    fun forwardMilliseconds(messageId: Int, millis: Long)
    fun changeSpeed(messageId: Int)
    fun resetMediaPlayer()
    fun setDuration(duration: Long)
    fun getCurrentPosition(): Int
    fun getMax(): Int
    fun getMessageId(): Int
    fun refreshSeekbarProgress()
    fun isPlaying(): Boolean
    fun completeAudioPlaying()
}