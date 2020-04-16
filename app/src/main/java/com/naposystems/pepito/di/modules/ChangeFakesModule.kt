package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.changeFakes.ChangeFakeDialogRepository
import com.naposystems.pepito.ui.changeFakes.IContractChangeFakes
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ChangeFakesModule {

    @Provides
    @Singleton
    fun provideRepository(
        contactDataSource: ContactDataSource
    ): IContractChangeFakes.Repository {
        return ChangeFakeDialogRepository(contactDataSource)
    }

}