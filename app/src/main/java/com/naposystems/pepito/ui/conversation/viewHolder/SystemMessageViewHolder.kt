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
import com.naposystems.pepito.databinding.ConversationItemSystemMessageBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SystemMessageViewHolder constructor(
    private val binding: ConversationItemSystemMessageBinding
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
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let { time ->
                val timeInDays = TimeUnit.SECONDS.toDays(time).toInt()
                when {
                    TimeUnit.SECONDS.toDays(time) > 1 -> {
                        textView?.text = textView?.resources?.getQuantityString(
                            R.plurals.text_self_destruct_time_days, timeInDays, timeInDays
                        )
                    }
                    TimeUnit.SECONDS.toDays(time) == 1L -> {
                        textView?.text =
                            textView?.resources?.getString(R.string.text_every_twenty_four_hours_min)
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
                    }
                }
            }
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
        clickListener: ConversationAdapter.ClickListener
    ) {
        val context = binding.container.context
        binding.clickListener = clickListener
        binding.conversation = item

        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            }
        )

        binding.cardView.setCardBackgroundColor(
            Utils.convertAttrToColorResource(
                context,
                R.attr.attrBackgroundColorPrimary
            )
        )

        val textColor = Utils.convertAttrToColorResource(
            context,
            R.attr.attrTextColorWhite
        )

        binding.textViewBody.setTextColor(textColor)
        binding.textViewCountDown.setTextColor(textColor)

        binding.textViewBody.text = item.message.body

        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): SystemMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemSystemMessageBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            return SystemMessageViewHolder(binding)
        }
    }
}