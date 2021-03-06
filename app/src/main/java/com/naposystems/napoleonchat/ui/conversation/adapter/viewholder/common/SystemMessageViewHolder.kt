package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.common

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemSystemMessageBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import java.util.concurrent.TimeUnit

class SystemMessageViewHolder constructor(
    private val binding: ConversationItemSystemMessageBinding
) : ConversationViewHolder(binding.root, binding.root.context) {

    init {
        super.parentContainerMessage = binding.containerMessage
    }

    private var countDownTimer: CountDownTimer? = null

    override fun countDown(
        item: MessageAttachmentRelation,
        textView: TextView?,
        itemToEliminate: (MessageAttachmentRelation) -> Unit
    ) {
        countDownTimer?.cancel()
        val endTime = item.messageEntity.totalSelfDestructionAt.toLong()
        if (endTime > 0) {
            val remainingTime =
                (endTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
            remainingTime.let { time ->
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

                        val text = Utils.getTimeWithDays(
                            millisUntilFinished,
                            showHours = true
                        )
                        textView?.text = text
                    }
                }
                countDownTimer?.start()
            }
        } else {
            showDestructionTime(item)
        }
    }

    private fun showDestructionTime(messageAndAttachmentRelation: MessageAttachmentRelation) {
        val message = messageAndAttachmentRelation.messageEntity
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

    override fun bind(
        item: MessageAttachmentRelation,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        val context = binding.textViewBody.context
        binding.clickListener = clickListener
        binding.conversation = item
        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            }
        )
        val textSystem = "${context.getString(R.string.text_new_contact)} "
        binding.textViewBody.text = textSystem
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