package com.naposystems.napoleonchat.di.module.repository

import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileSharedRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesSharedRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionSharedRepositoryImp
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileSharedRepository
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileSharedRepositoryImp
import dagger.Binds
import dagger.Module

@Module
abstract class SharedRepositoryModule {

    @Binds
    abstract fun bindContactSharedRepository(repository: ContactSharedRepositoryImp): ContactSharedRepository

    @Binds
    abstract fun bindContactProfileShareRepository(repository: ContactProfileSharedRepositoryImp): ContactProfileSharedRepository

    @Binds
    abstract fun bindFriendShipActionSharedRepository(repository: FriendShipActionSharedRepositoryImp): FriendShipActionSharedRepository

    @Binds
    abstract fun bindUserProfileSharedRepository(repository: UserProfileSharedRepositoryImp): UserProfileSharedRepository

    @Binds
    abstract fun bindDefaultPreferencesSharedRepository(repository: DefaultPreferencesSharedRepositoryImp): DefaultPreferencesSharedRepository

}