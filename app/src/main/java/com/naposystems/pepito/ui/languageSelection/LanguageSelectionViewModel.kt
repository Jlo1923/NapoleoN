package com.naposystems.pepito.ui.languageSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.repository.languageSelection.LanguageSelectionRepository
import javax.inject.Inject

class LanguageSelectionViewModel @Inject constructor(private val repository: LanguageSelectionRepository) :
    ViewModel(), IContractLanguageSelection.ViewModel {

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
