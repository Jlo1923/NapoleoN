package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.repository.defaultPreferences.DefaultPreferencesRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.IContractDefaultPreferences
import dagger.Module
import dagger.Provides

@Module
class DefaultPreferencesModule {

    @Provides
    fun provideRepository(
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractDefaultPreferences.Repository {
        return DefaultPreferencesRepository(context, sharedPreferencesManager)
    }

}