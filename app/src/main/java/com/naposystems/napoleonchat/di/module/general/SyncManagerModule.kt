package com.naposystems.napoleonchat.di.module.general

import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.service.syncManager.SyncManagerImp
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SyncManagerModule {

    @Binds
    @Singleton
    abstract fun bindSyncManager(syncManagerImp: SyncManagerImp): SyncManager

}