package com.naposystems.napoleonchat.ui.conversation.viewHolder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationItemGroupDateMessageBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GroupDateMessageViewHolder constructor(
    private val binding: ConversationItemGroupDateMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    fun bind(
        item: MessageAndAttachment
    ) {
        val context = binding.textViewBody.context
        val timeStamp =
            TimeUnit.SECONDS.toMillis(item.message.createdAt.toLong())
        val messageDate =
            Date(timeStamp)

        val timeActual = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayNext = sdf.format(Date((timeStamp + TimeUnit.DAYS.toMillis(1))))
        val dayActual = sdf.format(Date(timeActual))

        binding.conversation = item
        binding.textViewBody.text = when {
            item.message.body == dayActual -> {
                context.getString(R.string.text_date_today)
            }
            dayNext == dayActual -> {
                context.getString(R.string.text_yesterday)
            }
            else -> {
                sdf.format(messageDate)
            }
        } + " "
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