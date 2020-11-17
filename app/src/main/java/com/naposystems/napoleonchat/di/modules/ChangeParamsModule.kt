package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.repository.changeFakes.ChangeParamsDialogRepository
import com.naposystems.napoleonchat.ui.changeParams.IContractChangeParams
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