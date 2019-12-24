package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import com.naposystems.pepito.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RegisterRecoveryAccountModule {

    @Provides
    @Singleton
    fun provideRepository(sharedPreferencesManager: SharedPreferencesManager): IContractRegisterRecoveryAccount.Repository {
        return RegisterRecoveryAccountRepository(sharedPreferencesManager)
    }
}