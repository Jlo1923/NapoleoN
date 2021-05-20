package com.naposystems.napoleonchat.dialog.timeFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class TimeFormatDialogViewModel
@Inject constructor(
    private val repository: TimeFormatDialogRepository
) : ViewModel() {

    private val _timeFormat = MutableLiveData<Int>()
    val timeFormat: LiveData<Int>
        get() = _timeFormat

    fun setTimeFormat(format: Int) {
        _timeFormat.value = format
        repository.setTimeFormat(format)
    }

    fun getTimeFormat() {
        _timeFormat.value = repository.getTimeFormat()
    }

    fun getValTimeFormat(): Int? {
        return timeFormat.value
    }

}