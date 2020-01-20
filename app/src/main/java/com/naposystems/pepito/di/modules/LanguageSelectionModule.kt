package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.languageSelection.LanguageSelectionRepository
import dagger.Module
import dagger.Provides

@Module
class LanguageSelectionModule {

    @Provides
    fun bindRepository(context: Context): LanguageSelectionRepository {
         return LanguageSelectionRepository(context)
    }
}