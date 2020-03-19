package com.naposystems.pepito.ui.conversation.adapter

import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Utils
import java.util.concurrent.TimeUnit

open class ConversationViewHolder constructor(
    view: View
) : RecyclerView.ViewHolder(view) {

    private var countDownTimer: CountDownTimer? = null

    fun countDown (item: MessageAndAttachment,
             textView: TextView,
             itemToEliminate: (MessageAndAttachment) -> Unit) {

        countDownTimer?.cancel()
        val endTime = item.message.totalSelfDestructionAt.toLong()
        if (endTime > 0) {
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let { time ->
                val timeInDays = TimeUnit.SECONDS.toDays(time).toInt()
                when {
                    TimeUnit.SECONDS.toDays(time) >= 1 -> {
                        textView.text = textView.resources.getQuantityString(
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