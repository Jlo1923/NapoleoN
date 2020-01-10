package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.recoveryAccount.RecoveryAccountRepository
import com.naposystems.pepito.ui.recoveryAccount.IContractRecoveryAccount
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecoveryAccountModule {
    @Provides
    @Singleton
    fun provideRepository(napoleonApi: NapoleonApi): IContractRecoveryAccount.Repository {
        return RecoveryAccountRepository(napoleonApi)
    }
}