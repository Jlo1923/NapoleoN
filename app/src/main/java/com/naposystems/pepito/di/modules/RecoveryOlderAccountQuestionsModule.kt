package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.recoveryOlderAccountQuestionsRepository.RecoveryOlderAccountQuestionsRepository
import com.naposystems.pepito.ui.recoveryOlderAccountQuestions.IContractRecoveryOlderAccountQuestions
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RecoveryOlderAccountQuestionsModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        sharedPreferencesManager: SharedPreferencesManager,
        userLocalDataSource: UserLocalDataSource
    ): IContractRecoveryOlderAccountQuestions.Repository {
        return RecoveryOlderAccountQuestionsRepository(napoleonApi, sharedPreferencesManager, userLocalDataSource)
    }
}