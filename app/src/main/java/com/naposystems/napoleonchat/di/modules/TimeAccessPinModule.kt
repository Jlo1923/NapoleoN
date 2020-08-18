package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.timeAccessPin.TimeAccessPinRepository
import com.naposystems.napoleonchat.ui.timeAccessPin.IContractTimeAccessPin
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TimeAccessPinModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractTimeAccessPin.Repository {
        return TimeAccessPinRepository(sharedPreferencesManager)
    }
}