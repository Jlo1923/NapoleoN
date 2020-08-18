package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.languageSelection.LanguageSelectionRepository
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class LanguageSelectionModule {

    @Provides
    fun bindRepository(
        userLocalDataSource: UserLocalDataSource,
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi
    ): LanguageSelectionRepository {
        return LanguageSelectionRepository(
            userLocalDataSource,
            context,
            sharedPreferencesManager,
            napoleonApi
        )
    }
}