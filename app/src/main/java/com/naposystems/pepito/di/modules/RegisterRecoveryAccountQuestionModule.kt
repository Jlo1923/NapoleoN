package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.pepito.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RegisterRecoveryAccountQuestionModule {

    @Provides
    @Singleton
    fun provideRepository(napoleonApi: NapoleonApi, sharedPreferencesManager: SharedPreferencesManager): IContractRegisterRecoveryAccountQuestion.Repository {
        return RegisterRecoveryAccountQuestionRepository(napoleonApi, sharedPreferencesManager)
    }
}