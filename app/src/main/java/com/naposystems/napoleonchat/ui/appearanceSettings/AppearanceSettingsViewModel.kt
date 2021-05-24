package com.naposystems.napoleonchat.ui.appearanceSettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppearanceSettingsViewModel @Inject constructor(
    private val repository: AppearanceSettingsRepository
) : ViewModel() {

    private val _colorScheme = MutableLiveData<Int>()
    val colorScheme: LiveData<Int>
        get() = _colorScheme

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    private val _timeFormat = MutableLiveData<Int>()
    val timeFormat: LiveData<Int>
        get() = _timeFormat

    private val _conversationBackground = MutableLiveData<String>()
    val conversationBackground: LiveData<String>
        get() = _conversationBackground

    init {
        _colorScheme.value = null
        _userDisplayFormat.value = null
        _timeFormat.value = null
        _conversationBackground.value = null
    }

    //region Implementation IContractAppearanceSettings.ViewModel
    fun getColorScheme() {
        _colorScheme.value = repository.getColorScheme()
    }

    fun getUserDisplayFormat() {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    fun getTimeFormat() {
        _timeFormat.value = repository.getTimeFormat()
    }

    fun getConversationBackground() {
        viewModelScope.launch {
            _conversationBackground.value = repository.getConversationBackground()
        }
    }

    fun resetConversationBackgroundLiveData() {
        _conversationBackground.value = null
    }

    //endregion
}
