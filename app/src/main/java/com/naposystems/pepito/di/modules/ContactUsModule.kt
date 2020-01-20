package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.contactUs.ContactUsRepository
import com.naposystems.pepito.ui.contactUs.IContractContactUs
import com.naposystems.pepito.webService.NapoleonApi
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