package com.naposystems.napoleonchat.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemGroupDateMessageBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GroupDateMessageViewHolder constructor(
    private val binding: ConversationItemGroupDateMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    fun bind(
        item: MessageAttachmentRelation
    ) {
        val context = binding.textViewBody.context
        val timeStamp =
            TimeUnit.SECONDS.toMillis(item.messageEntity.createdAt.toLong())
        val messageDate =
            Date(timeStamp)

        val timeActual = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayNext = sdf.format(Date((timeStamp + TimeUnit.DAYS.toMillis(1))))
        val dayActual = sdf.format(Date(timeActual))

        binding.conversation = item
        binding.textViewBody.text = when {
            item.messageEntity.body == dayActual -> {
                context.getString(R.string.text_date_today)
            }
            dayNext == dayActual -> {
                context.getString(R.string.text_yesterday)
            }
            else -> {
                sdf.format(messageDate)
            }
        } + context.getString(R.string.unicode_no_break_space)
        binding.executePendingBindings()
    }

    companion object {
        fun from(
            parent: ViewGroup
        ): GroupDateMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ConversationItemGroupDateMessageBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            return GroupDateMessageViewHolder(binding)
        }
    }
}