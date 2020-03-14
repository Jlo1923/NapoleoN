package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountRepository
import com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount.IContractValidatePasswordPreviousRecoveryAccount
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ValidatePasswordPreviousRecoveryAccountModule {

    @Singleton
    @Provides
    fun provideRepository(
        napoleonApi: NapoleonApi, sharedPreferencesManager: SharedPreferencesManager
    ): IContractValidatePasswordPreviousRecoveryAccount.Repository {
        return ValidatePasswordPreviousRecoveryAccountRepository(napoleonApi, sharedPreferencesManager)
    }
}