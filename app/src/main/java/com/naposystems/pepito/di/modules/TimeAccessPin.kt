package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.timeAccessPin.TimeAccessPinRepository
import com.naposystems.pepito.ui.timeAccessPin.IContractTimeAccessPin
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TimeAccessPin {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractTimeAccessPin.Repository {
        return TimeAccessPinRepository(sharedPreferencesManager)
    }
}