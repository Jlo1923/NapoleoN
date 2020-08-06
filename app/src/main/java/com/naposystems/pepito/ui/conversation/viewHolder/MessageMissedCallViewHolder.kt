package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemMissedCallBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MessageMissedCallViewHolder constructor(
    private val binding: ConversationItemMissedCallBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null

    private fun countDown(
        item: MessageAndAttachment,
        textView: TextView?,
        itemToEliminate: (MessageAndAttachment) -> Unit
    ) {
        countDownTimer?.cancel()
        val endTime = item.message.totalSelfDestructionAt.toLong()
        if (endTime > 0) {
            countDownTimer = object : CountDownTimer(
                TimeUnit.SECONDS.toMillis(endTime) - System.currentTimeMillis(),
                1
            ) {
                override fun onFinish() {
                    itemToEliminate(item)
                }

                override fun onTick(millisUntilFinished: Long) {
                    if (textView?.isVisible == false) {
                        textView.visibility = View.VISIBLE
                    }
                    textView?.text = Utils.getDuration(
                        millisUntilFinished,
                        showHours = false
                    )
                }
            }
            countDownTimer?.start()
        } else {
            showDestructionTime(item)
        }
    }

    private fun showDestructionTime(messageAndAttachment: MessageAndAttachment) {
        val message = messageAndAttachment.message
        val stringId = when (message.selfDestructionAt) {
            Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time -> R.string.text_every_five_seconds
            Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time -> R.string.text_every_fifteen_seconds
            Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time -> R.string.text_every_thirty_seconds
            Constants.SelfDestructTime.EVERY_ONE_MINUTE.time -> R.string.text_every_one_minute
            Constants.SelfDestructTime.EVERY_TEN_MINUTES.time -> R.string.text_every_ten_minutes
            Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time -> R.string.text_every_thirty_minutes
            Constants.SelfDestructTime.EVERY_ONE_HOUR.time -> R.string.text_every_one_hour
            Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time -> R.string.text_every_twelve_hours
            Constants.SelfDestructTime.EVERY_ONE_DAY.time -> R.string.text_every_twenty_four_hours_min
            Constants.SelfDestructTime.EVERY_SEVEN_DAY.time -> R.string.text_every_seven_days
            else -> -1
        }
        if (stringId != -1) {
            val context = binding.textViewCountDown.context
            binding.textViewCountDown.visibility = View.VISIBLE
            binding.textViewCountDown.text = context.resources.getString(stringId)
        }
    }

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        timeFormat: Int?
    ) {
        val context = binding.containerMissedCall.context
        binding.clickListener = clickListener
        binding.conversation = item

        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            }
        )

        try {
            val sdf = if (timeFormat == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("hh:mm aa", Locale.getDefault())
            }
            val netDate = Date(item.message.createdAt.toLong() * 1000)

            val formatDate = sdf.format(netDate)

            val iconId: Int
            val stringId: Int

            when (item.message.messageType) {
                Constants.MessageType.MISSED_CALL.type -> {
                    iconId = R.drawable.ic_call_missed_red
                    stringId = R.string.text_missed_voice_call_at
                }
                else -> {
                    iconId = R.drawable.ic_videocall_missed_red
                    stringId = R.string.text_missed_video_call_at
                }
            }

            binding.textViewBody.text = context.getString(stringId, formatDate)
            binding.imageViewIcon.setImageResource(iconId)

        } catch (e: Exception) {
            Timber.e("Error parsing date")
        }

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): MessageMissedCallViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemMissedCallBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            return MessageMissedCallViewHolder(binding)
        }
    }
}