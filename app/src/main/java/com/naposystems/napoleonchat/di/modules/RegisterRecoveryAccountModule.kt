package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
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