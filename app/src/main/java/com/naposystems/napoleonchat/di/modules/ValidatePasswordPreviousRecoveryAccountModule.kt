package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountRepository
import com.naposystems.napoleonchat.ui.validatePasswordPreviousRecoveryAccount.IContractValidatePasswordPreviousRecoveryAccount
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
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