package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecoveryAccountQuestionsModule {
    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager,
        userLocalDataSource: UserLocalDataSource
    ): IContractRecoveryAccountQuestions.Repository {
        return RecoveryAccountQuestionsRepository(
            napoleonApi,
            sharedPreferencesManager,
            userLocalDataSource
        )
    }
}