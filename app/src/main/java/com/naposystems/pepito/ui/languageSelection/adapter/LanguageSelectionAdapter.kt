package com.naposystems.pepito.ui.languageSelection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.ListItemLanguageSelectionBinding
import com.naposystems.pepito.model.languageSelection.Language

class LanguageSelectionAdapter(
    private val languages: List<Language>,
    private val clickListener: LanguageSelectionListener
) :
    RecyclerView.Adapter<LanguageSelectionAdapter.LanguageViewHolder>() {

    private var selectedLanguage: Language? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder.from(parent)
    }

    override fun getItemCount() = languages.size

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(clickListener, language, selectedLanguage)
    }

    class LanguageViewHolder private constructor(private val binding: ListItemLanguageSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: LanguageSelectionListener, language: Language, selectedLanguage: Language?) {
            binding.language = language
            binding.radioButtonLanguage.isChecked = language.id == selectedLanguage?.id
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): LanguageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ListItemLanguageSelectionBinding.inflate(layoutInflater, parent, false)
                return LanguageViewHolder(binding)
            }
        }
    }

    class LanguageSelectionListener(val clickListener: (language: Language) -> Unit) {
        fun onClick(language: Language) = clickListener(language)
    }

    fun updateSelectedLanguage(language: Language){
        selectedLanguage = language
        notifyDataSetChanged()
    }
}