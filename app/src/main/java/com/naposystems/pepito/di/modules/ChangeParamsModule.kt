package com.naposystems.pepito.di.modules

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.repository.changeFakes.ChangeParamsDialogRepository
import com.naposystems.pepito.ui.changeParams.IContractChangeParams
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ChangeParamsModule {

    @Provides
    @Singleton
    fun provideRepository(
        contactDataSource: ContactDataSource
    ): IContractChangeParams.Repository {
        return ChangeParamsDialogRepository(contactDataSource)
    }

}