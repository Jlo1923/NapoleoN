package com.naposystems.pepito.ui.languageSelection

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.repository.languageSelection.LanguageSelectionRepository

class LanguageSelectionViewModel(context: Context) : ViewModel(), IContractLanguageSelection.ViewModel {

    private val repository by lazy {
        LanguageSelectionRepository(context)
    }

    val languagesList = getLanguages()

    private val _selectedLanguage = MutableLiveData<Language>()
    val selectedLanguage: LiveData<Language>
        get() = _selectedLanguage

    fun setSelectedLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    init {
        repository.getLanguages()
    }

    override fun getLanguages() = repository.getLanguages()
}
