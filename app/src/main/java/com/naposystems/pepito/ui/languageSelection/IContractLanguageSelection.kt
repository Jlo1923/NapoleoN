package com.naposystems.pepito.ui.languageSelection

import com.naposystems.pepito.model.languageSelection.Language

interface IContractLanguageSelection {

    interface ViewModel {
        fun getLanguages(): List<Language>
    }

    interface Repository {
        fun getLanguages(): List<Language>
    }
}