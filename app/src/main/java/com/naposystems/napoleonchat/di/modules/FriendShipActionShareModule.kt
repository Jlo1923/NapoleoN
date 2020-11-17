package com.naposystems.napoleonchat.di.modules

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.repository.sharedRepository.FriendShipActionShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.IContractFriendShipAction
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FriendShipActionShareModule {

    @Provides
    @Singleton
    fun provideRepository(
        napoleonApi: NapoleonApi,
        contactLocalDataSource: ContactDataSource,
        messageLocalDataSource: MessageDataSource
    ): IContractFriendShipAction.Repository {
        return FriendShipActionShareRepository(
            napoleonApi,
            contactLocalDataSource,
            messageLocalDataSource
        )
    }
}