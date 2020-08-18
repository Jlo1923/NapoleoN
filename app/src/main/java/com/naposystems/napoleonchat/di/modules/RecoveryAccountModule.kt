package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.recoveryAccount.RecoveryAccountRepository
import com.naposystems.napoleonchat.ui.recoveryAccount.IContractRecoveryAccount
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecoveryAccountModule {
    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager
    ): IContractRecoveryAccount.Repository {
        return RecoveryAccountRepository(napoleonApi, sharedPreferencesManager)
    }
}