package com.naposystems.napoleonchat.ui.dialog.timeFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class TimeFormatDialogViewModel @Inject constructor(
    private val dialogRepositoryImp: TimeFormatDialogRepositoryImp
) : ViewModel() {

    private val _timeFormat = MutableLiveData<Int>()
    val timeFormat: LiveData<Int>
        get() = _timeFormat

    fun setTimeFormat(format: Int) {
        _timeFormat.value = format
        dialogRepositoryImp.setTimeFormat(format)
    }

    fun getTimeFormat() {
        _timeFormat.value = dialogRepositoryImp.getTimeFormat()
    }

    fun getValTimeFormat(): Int? {
        return timeFormat.value
    }

}