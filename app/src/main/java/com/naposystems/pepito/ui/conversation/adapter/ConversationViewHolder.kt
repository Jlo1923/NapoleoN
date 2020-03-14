package com.naposystems.pepito.ui.conversation.adapter

import android.content.res.Resources
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Utils
import java.util.concurrent.TimeUnit

open class ConversationViewHolder constructor(
    view: View,
    var countDownTimer: CountDownTimer?
) : RecyclerView.ViewHolder(view) {

    fun countDown (item: MessageAndAttachment,
             endTime: Long,
             textView: TextView,
             resources: Resources,
             itemToEliminate: (MessageAndAttachment) -> Unit) {

        countDownTimer?.cancel()
        if (endTime > 0) {
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let { time ->
                val timeInDays = TimeUnit.SECONDS.toDays(time).toInt()
                when {
                    TimeUnit.SECONDS.toDays(time) >= 1 -> {
                        textView.text = resources.getQuantityString(
                            R.plurals.text_self_destruct_time_days, timeInDays, timeInDays
                        )
                    }
                    else -> {
                        countDownTimer = object : CountDownTimer(
                            TimeUnit.SECONDS.toMillis(endTime) - System.currentTimeMillis(),
                            1
                        ) {
                            override fun onFinish() {
                                itemToEliminate(item)
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                textView.text = Utils.getDuration(
                                    millisUntilFinished,
                                    showHours = false
                                )
                            }
                        }
                        countDownTimer?.start()
                    }
                }
            }
        } else {
            textView.text = ""
        }

    }

}