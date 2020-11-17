package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.webRTCCallService.WebRTCCallServiceRepository
import com.naposystems.napoleonchat.service.webRTCCall.IContractWebRTCCallService
import com.naposystems.napoleonchat.webService.NapoleonApi
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