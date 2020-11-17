package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepository
import com.naposystems.napoleonchat.ui.contactUs.IContractContactUs
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContactUsModule {

    @Provides
    @Singleton
    fun provideRepository(napoleonApi: NapoleonApi): IContractContactUs.Repository {
        return ContactUsRepository(napoleonApi)
    }
}