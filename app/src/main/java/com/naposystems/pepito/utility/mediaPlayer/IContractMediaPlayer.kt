package com.naposystems.pepito.utility.mediaPlayer

import android.net.Uri
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.security.crypto.EncryptedFile
import com.naposystems.pepito.ui.custom.animatedTwoVectorView.AnimatedTwoVectorView

interface IContractMediaPlayer {
    fun playAudio(audioId: Int, uri: Uri)
    fun playAudio(audioId: Int, fileName: String)
    fun pauseAudio()
    fun registerProximityListener()
    fun unregisterProximityListener()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setImageButtonPlay(imageButtonPlay: AnimatedTwoVectorView)
    fun setImageButtonSpeed(imageButtonSpeed: ImageButton)
    fun setSeekbar(seekBar: AppCompatSeekBar)
    fun setTextViewDuration(textView: TextView)
    fun setListener(listener: MediaPlayerManager.Listener)
    fun rewindMilliseconds(audioId: Int, millis: Long)
    fun forwardMilliseconds(audioId: Int, millis: Long)
    fun changeSpeed(audioId: Int)
    fun resetMediaPlayer()
}