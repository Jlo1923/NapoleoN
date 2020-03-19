package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.status.StatusLocalDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.repository.status.StatusRepository
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides

@Module
class StatusModule {

    @Provides
    fun provideRepository(
        statusLocalDataSource: StatusLocalDataSource,
        napoleonApi: NapoleonApi,
        userLocalDataSource: UserLocalDataSource
    ): StatusRepository {
        return StatusRepository(statusLocalDataSource, napoleonApi, userLocalDataSource)
    }
}