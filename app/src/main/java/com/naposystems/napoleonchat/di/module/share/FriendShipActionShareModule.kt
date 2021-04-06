package com.naposystems.napoleonchat.di.module.share

import com.naposystems.napoleonchat.repository.sharedRepository.FriendShipActionShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.IContractFriendShipAction
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class FriendShipActionShareModule {

    @Binds
    @Singleton
    abstract fun bindFriendShipActionShareRepository(
        repository: FriendShipActionShareRepository
    ): IContractFriendShipAction.Repository
}