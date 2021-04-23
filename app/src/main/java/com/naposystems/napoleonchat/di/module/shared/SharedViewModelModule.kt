package com.naposystems.napoleonchat.di.module.shared

import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.camera.CameraShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SharedViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserDisplayFormatShareViewModel::class)
    internal abstract fun bindUserDisplayFormatShareViewModel(viewModel: UserDisplayFormatShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeFormatShareViewModel::class)
    internal abstract fun bindTimeFormatShareViewModel(viewModel: TimeFormatShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileShareViewModel::class)
    internal abstract fun bindUserProfileShareViewModel(viewModel: UserProfileShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactSharedViewModel::class)
    internal abstract fun bindShareContactViewModel(viewModel: ContactSharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationShareViewModel::class)
    internal abstract fun bindConversationShareViewModel(viewModel: ConversationShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactProfileShareViewModel::class)
    internal abstract fun bindContactProfileShareViewModel(viewModel: ContactProfileShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactRepositoryShareViewModel::class)
    internal abstract fun bindContactRepositoryShareViewModel(viewModel: ContactRepositoryShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GalleryShareViewModel::class)
    internal abstract fun bindGalleryShareViewModel(viewModel: GalleryShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CameraShareViewModel::class)
    internal abstract fun bindCameraShareViewModel(viewModel: CameraShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendShipActionShareViewModel::class)
    internal abstract fun bindFriendShipActionShareViewModel(viewModel: FriendShipActionShareViewModel): ViewModel

}