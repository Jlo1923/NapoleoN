package com.naposystems.napoleonchat.di.module.shared

import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareRepository
import com.naposystems.napoleonchat.repository.sharedRepository.FriendShipActionShareRepository
import com.naposystems.napoleonchat.repository.sharedRepository.UserProfileShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.IContractFriendShipAction
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.IContractUserProfileShare
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SharedRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShareContactRepository(repository: ContactSharedRepositoryImp): ContactSharedRepository

    @Binds
    @Singleton
    abstract fun bindContactProfileShareRepository(repository: ContactProfileShareRepositoryImp): ContactProfileShareRepository
    @Binds
    @Singleton
    abstract fun bindContactRepositoryShareRepository(
        repository: ContactRepositoryShareRepository
    ): IContractContactRepositoryShare.Repository


    @Binds
    @Singleton
    abstract fun bindFriendShipActionShareRepository(
        repository: FriendShipActionShareRepository
    ): IContractFriendShipAction.Repository

    @Binds
    @Singleton
    abstract fun bindUserProfileShareRepository(
        repository: UserProfileShareRepository
    ): IContractUserProfileShare.Repository

}