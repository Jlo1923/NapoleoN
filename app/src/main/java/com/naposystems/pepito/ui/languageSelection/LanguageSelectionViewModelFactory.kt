package com.naposystems.pepito.ui.languageSelection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LanguageSelectionViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageSelectionViewModel::class.java)) {
            return LanguageSelectionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}