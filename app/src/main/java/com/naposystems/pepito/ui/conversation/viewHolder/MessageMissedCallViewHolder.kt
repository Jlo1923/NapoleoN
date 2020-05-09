package com.naposystems.pepito.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationItemMissedCallBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.utility.Constants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MessageMissedCallViewHolder constructor(
    private val binding: ConversationItemMissedCallBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("ResourceAsColor")
    fun bind(
        item: MessageAndAttachment,
        clickListener: ConversationAdapter.ClickListener,
        timeFormat: Int?
    ) {
        val context = binding.containerMissedCall.context
        binding.clickListener = clickListener
        binding.conversation = item

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