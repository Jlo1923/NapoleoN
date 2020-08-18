package com.naposystems.napoleonchat.ui.napoleonKeyboardEmojiPage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.emoji.text.EmojiCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardEmojiPageFragmentItemBinding
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji

class NapoleonKeyboardEmojiPageAdapter constructor(private val listener: OnNapoleonKeyboardEmojiPageAdapterListener) :
    ListAdapter<Emoji, NapoleonKeyboardEmojiPageAdapter.EmojiViewHolder>(Emoji.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        return EmojiViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = getItem(position)
        holder.bind(emoji, listener)
    }

    class EmojiViewHolder constructor(private val binding: NapoleonKeyboardEmojiPageFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: Emoji, listener: OnNapoleonKeyboardEmojiPageAdapterListener) {
            binding.textViewEmoji.text =
                EmojiCompat.get().process(String(emoji.code, 0, emoji.code.size))

            binding.textViewEmoji.setOnClickListener {
                listener.onEmojiClick(emoji)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EmojiViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NapoleonKeyboardEmojiPageFragmentItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return EmojiViewHolder(binding)
            }
        }
    }

    interface OnNapoleonKeyboardEmojiPageAdapterListener {
        fun onEmojiClick(emoji: Emoji)
    }
}