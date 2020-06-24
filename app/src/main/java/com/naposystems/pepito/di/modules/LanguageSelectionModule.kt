package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.languageSelection.LanguageSelectionRepository
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
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