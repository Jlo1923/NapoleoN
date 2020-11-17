package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.activateBiometrics.ActivateBiometricsRepository
import com.naposystems.napoleonchat.ui.activateBiometrics.IContractActivateBiometrics
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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