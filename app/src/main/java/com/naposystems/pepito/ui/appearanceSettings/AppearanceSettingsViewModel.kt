package com.naposystems.pepito.ui.appearanceSettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.repository.appearanceSettings.AppearanceSettingsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AppearanceSettingsViewModel @Inject constructor(
    private val repository: AppearanceSettingsRepository
) : ViewModel(), IContractAppearanceSettings.ViewModel {

    private val _colorScheme = MutableLiveData<Int>()
    val colorScheme: LiveData<Int>
        get() = _colorScheme

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    private val _chatBackgroundUpdated = MutableLiveData<Boolean>()
    val chatBackgroundUpdated: LiveData<Boolean>
        get() = _chatBackgroundUpdated

    init {
        _colorScheme.value = null
        _userDisplayFormat.value = null
        _chatBackgroundUpdated.value = null
    }

    fun resetChatBackgroundUpdated() {
        _chatBackgroundUpdated.value = null
    }

    //region Implementation IContractAppearanceSettings.ViewModel
    override fun getColorScheme() {
        _colorScheme.value = repository.getColorScheme()
    }

    override fun getUserDisplayFormat() {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    override fun updateChatBackground(uri: String) {
        viewModelScope.launch {
            try {
                repository.updateChatBackground(uri)
                _chatBackgroundUpdated.value = true
            } catch (e: Exception) {
                _chatBackgroundUpdated.value = false
                Timber.e(e)
            }
        }
    }

    //endregion
}
