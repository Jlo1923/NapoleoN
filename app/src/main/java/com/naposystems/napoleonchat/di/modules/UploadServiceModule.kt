package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.repository.uploadService.UploadServiceRepository
import com.naposystems.napoleonchat.service.uploadService.IContractUploadService
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class UploadServiceModule {

    @Binds
    abstract fun provideRepository(repository: UploadServiceRepository): IContractUploadService.Repository
}