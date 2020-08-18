package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
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