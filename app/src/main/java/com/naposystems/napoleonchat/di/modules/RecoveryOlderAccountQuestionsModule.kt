package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.recoveryOlderAccountQuestionsRepository.RecoveryOlderAccountQuestionsRepository
import com.naposystems.napoleonchat.ui.recoveryOlderAccountQuestions.IContractRecoveryOlderAccountQuestions
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
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