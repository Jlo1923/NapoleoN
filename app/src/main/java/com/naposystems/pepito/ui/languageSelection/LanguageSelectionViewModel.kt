package com.naposystems.pepito.ui.languageSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.repository.languageSelection.LanguageSelectionRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class LanguageSelectionViewModel @Inject constructor(private val repository: LanguageSelectionRepository) :
    ViewModel(), IContractLanguageSelection.ViewModel {

    val languagesList = getLanguages()

    private val _selectedLanguage = MutableLiveData<Language>()
    val selectedLanguage: LiveData<Language>
        get() = _selectedLanguage

    private val _errorUpdatingLanguage = MutableLiveData<Boolean>()
    val errorUpdatingLanguage: LiveData<Boolean>
        get() = _errorUpdatingLanguage

    fun setSelectedLanguage(language: Language) {
        viewModelScope.launch {
            try {
                val response = repository.updateUserLanguage(language)

                if (response.isSuccessful) {
                    repository.updateUserLanguagePreference(language.iso)
                    _selectedLanguage.value = language
                } else {
                    _errorUpdatingLanguage.value = true
                }
            } catch (e: Exception) {
                _errorUpdatingLanguage.value = true
            }
        }
    }

    init {
        repository.getLanguages()
    }

    override fun getLanguages() = repository.getLanguages()

    override fun resetErrorUpdatingLanguage() {
        _errorUpdatingLanguage.value = null
    }
}
