package com.naposystems.pepito.ui.emojiKeyboardPage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.emoji.text.EmojiCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.EmojiKeyboardPageFragmentItemBinding
import com.naposystems.pepito.model.emojiKeyboard.Emoji
import timber.log.Timber
import java.nio.charset.Charset

class EmojiKeyboardPageAdapter :
    ListAdapter<Emoji, EmojiKeyboardPageAdapter.EmojiViewHolder>(Emoji.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = getItem(position)
        holder.bind(emoji)
    }

    class EmojiViewHolder constructor(private val binding: EmojiKeyboardPageFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: Emoji) {
            binding.textViewEmoji.text =
                EmojiCompat.get().process(String(Character.toChars(emoji.code)))

            binding.textViewEmoji.setOnClickListener {
                Timber.d(emoji.toString())
                Toast.makeText(binding.textViewEmoji.context, emoji.name, Toast.LENGTH_SHORT).show()
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EmojiViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EmojiKeyboardPageFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return EmojiViewHolder(binding)
            }
        }

    }
}