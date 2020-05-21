package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.webRTCCallService.WebRTCCallServiceRepository
import com.naposystems.pepito.service.webRTCCall.IContractWebRTCCallService
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class WebRTCCallServiceModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi
    ): IContractWebRTCCallService.Repository {
        return WebRTCCallServiceRepository(
            napoleonApi
        )
    }
}