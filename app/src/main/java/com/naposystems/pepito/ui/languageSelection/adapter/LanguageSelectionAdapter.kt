package com.naposystems.pepito.ui.languageSelection.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.LanguageSelectionDialogFragmentItemBinding
import com.naposystems.pepito.model.languageSelection.Language

class LanguageSelectionAdapter(
    private val languages: List<Language>,
    private val clickListener: LanguageSelectionListener,
    private val languageSelected: String
) :
    RecyclerView.Adapter<LanguageSelectionAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder.from(parent)
    }

    override fun getItemCount() = languages.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(clickListener, language, languageSelected)
    }

    class LanguageViewHolder private constructor(private val binding: LanguageSelectionDialogFragmentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: LanguageSelectionListener,
            language: Language,
            languageSelected: String
        ) {
            binding.language = language
            if (language.iso == languageSelected) {
                binding.imageViewSelected.visibility = View.VISIBLE
            }
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): LanguageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    LanguageSelectionDialogFragmentItemBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                return LanguageViewHolder(binding)
            }
        }
    }

    class LanguageSelectionListener(val clickListener: (language: Language) -> Unit) {
        fun onClick(language: Language) = clickListener(language)
    }
}