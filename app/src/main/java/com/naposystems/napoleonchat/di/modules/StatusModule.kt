package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.status.StatusLocalDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.repository.status.StatusRepository
import com.naposystems.napoleonchat.webService.NapoleonApi
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