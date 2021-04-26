package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.common

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemMissedCallBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationViewHolder
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MessageMissedCallViewHolder constructor(
    private val binding: ConversationItemMissedCallBinding
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

    /*@SuppressLint("ResourceAsColor")
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
            val messageTime = item.message.createdAt.toLong() * 1000

            val netDate = Date(messageTime)

            val sdfCompare = SimpleDateFormat("dd", Locale.getDefault())

            val compareDate = Date(System.currentTimeMillis())

            val addDate =
                if (
                    sdfCompare.format(compareDate).toLong() > sdfCompare.format(netDate).toLong()
                ) "dd/MM/yyyy  "
                else ""

            val sdf = if (timeFormat == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                SimpleDateFormat(addDate + "HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat(addDate + "hh:mm aa", Locale.getDefault())
            }

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
    }*/

    override fun bind(
        item: MessageAttachmentRelation,
        clickListener: ConversationAdapter.ClickListener,
        isFirst: Boolean,
        timeFormat: Int?,
        mediaPlayerManager: MediaPlayerManager?
    ) {
        super.bind(item, clickListener, isFirst, timeFormat, mediaPlayerManager)
        val context = binding.containerMessage.context
        binding.conversation = item
        binding.clickListener = clickListener
        countDown(
            item,
            binding.textViewCountDown,
            itemToEliminate = { messageAndAttachment ->
                clickListener.messageToEliminate(messageAndAttachment)
            }
        )

        try {
            val messageTime = item.messageEntity.createdAt.toLong() * 1000
            val netDate = Date(messageTime)
            val sdfCompare = SimpleDateFormat("dd", Locale.getDefault())
            val compareDate = Date(System.currentTimeMillis())
            val addDate =
                if (
                    sdfCompare.format(compareDate).toLong() > sdfCompare.format(netDate).toLong()
                ) "dd/MM/yyyy  "
                else ""

            val sdf = if (timeFormat == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                SimpleDateFormat(addDate + "HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat(addDate + "hh:mm aa", Locale.getDefault())
            }

            val formatDate = sdf.format(netDate)

            val iconId: Int
            val stringId: Int

            when (item.messageEntity.messageType) {
                Constants.MessageTextType.MISSED_CALL.type -> {
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