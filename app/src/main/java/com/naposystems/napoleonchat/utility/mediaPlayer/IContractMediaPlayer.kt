package com.naposystems.napoleonchat.utility.mediaPlayer

import android.content.Context
import android.net.Uri
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar

interface IContractMediaPlayer {
    fun setContext(context: Context)
    fun initializeBluetoothManager()
    fun setAudioId(audioId: String)
    fun setAudioUri(uri: Uri?)
    fun setAudioFileName(fileName: String)
    fun setWebId(webId : String?)
    fun playAudio(progress: Int = 0, isEarpiece: Boolean = false)
    fun pauseAudio()
    fun registerProximityListener()
    fun unregisterProximityListener()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setImageButtonPlay(imageButtonPlay: ImageView)
    fun setImageButtonSpeed(imageButtonSpeed: ImageButton)
    fun setStateImageButtonSpeed(imageButtonSpeed: ImageButton, webId : String)
    fun setSeekbar(seekBar: AppCompatSeekBar)
    fun setTextViewDuration(textView: TextView)
    fun setListener(listener: MediaPlayerManager.Listener)
    fun rewindMilliseconds(audioId: String, millis: Long)
    fun forwardMilliseconds(audioId: String, millis: Long)
    fun changeSpeed(audioId: String)
    fun resetMediaPlayer()
    fun resetMediaPlayer(id: String)
    fun setDuration(duration: Long)
    fun getCurrentPosition(): Int
    fun getMax(): Int
    fun getAudioId(): String
    fun refreshSeekbarProgress()
    fun isPlaying(): Boolean
}