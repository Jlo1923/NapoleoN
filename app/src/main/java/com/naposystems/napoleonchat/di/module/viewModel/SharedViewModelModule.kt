package com.naposystems.napoleonchat.di.module.viewModel

import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.CameraSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.GallerySharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileSharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SharedViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileSharedViewModel::class)
    internal abstract fun bindUserProfileShareViewModel(viewModel: UserProfileSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactSharedViewModel::class)
    internal abstract fun bindShareContactViewModel(viewModel: ContactSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationSharedViewModel::class)
    internal abstract fun bindConversationShareViewModel(viewModel: ConversationSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactProfileSharedViewModel::class)
    internal abstract fun bindContactProfileShareViewModel(viewModel: ContactProfileSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GallerySharedViewModel::class)
    internal abstract fun bindGalleryShareViewModel(viewModel: GallerySharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CameraSharedViewModel::class)
    internal abstract fun bindCameraShareViewModel(viewModel: CameraSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendShipActionSharedViewModel::class)
    internal abstract fun bindFriendShipActionSharedViewModel(viewModel: FriendShipActionSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DefaultPreferencesSharedViewModel::class)
    internal abstract fun bindDefaultPreferencesSharedViewModel(viewModel: DefaultPreferencesSharedViewModel): ViewModel

}