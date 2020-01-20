package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.pepito.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
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