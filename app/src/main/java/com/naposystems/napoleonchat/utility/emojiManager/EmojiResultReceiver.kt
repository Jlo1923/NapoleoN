package com.naposystems.napoleonchat.utility.emojiManager

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class EmojiResultReceiver constructor(handler: Handler) : ResultReceiver(handler) {

    private var listener: Listener? = null

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        listener?.onReceivedResult(resultCode, resultData)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onReceivedResult(resultCode: Int, data: Bundle?)
    }
}