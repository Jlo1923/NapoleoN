package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.activateBiometrics.ActivateBiometricsRepository
import com.naposystems.pepito.ui.activateBiometrics.IContractActivateBiometrics
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ActivateBiometricsModule {
    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractActivateBiometrics.Repository {
        return ActivateBiometricsRepository(sharedPreferencesManager)
    }
}