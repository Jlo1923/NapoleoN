package com.naposystems.napoleonchat.dialog.languageSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.languageSelection.Language
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class LanguageSelectionDialogViewModel
@Inject constructor(
    private val repository: LanguageSelectionDialogRepositoryImp
) : ViewModel() {

    val languagesList = getLanguages()

    private val _selectedLanguage = MutableLiveData<Language>()
    val selectedLanguage: LiveData<Language>
        get() = _selectedLanguage

    private val _errorUpdatingLanguage = MutableLiveData<Boolean>()
    val errorUpdatingLanguage: LiveData<Boolean>
        get() = _errorUpdatingLanguage

    init {
        repository.getLanguages()
    }

     fun setSelectedLanguage(language: Language, location: Int) {
        viewModelScope.launch {
            try {
                when (location) {
                    Constants.LocationSelectionLanguage.LANDING.location -> {
                        updateLanguageLocal(language)
                    }
                    else -> {
                        val response = repository.updateUserLanguage(language)

                        if (response.isSuccessful) {
                            updateLanguageLocal(language)
                        } else {
                            _errorUpdatingLanguage.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _errorUpdatingLanguage.value = true
            }
        }
    }

     fun getLanguages() = repository.getLanguages()

     suspend fun updateLanguageLocal(language: Language) {
        repository.updateUserLanguagePreference(language.iso)
        _selectedLanguage.value = language
    }

     fun resetErrorUpdatingLanguage() {
        _errorUpdatingLanguage.value = null
    }
}
