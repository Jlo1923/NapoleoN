package com.naposystems.napoleonchat.di.module.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naposystems.napoleonchat.ui.accountAttack.AccountAttackDialogViewModel
import com.naposystems.napoleonchat.ui.activateBiometrics.ActivateBiometricsViewModel
import com.naposystems.napoleonchat.ui.addContact.AddContactViewModel
import com.naposystems.napoleonchat.ui.appearanceSettings.AppearanceSettingsViewModel
import com.naposystems.napoleonchat.ui.attachmentAudio.AttachmentAudioViewModel
import com.naposystems.napoleonchat.ui.attachmentGallery.AttachmentGalleryViewModel
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.AttachmentGalleryFoldersViewModel
import com.naposystems.napoleonchat.ui.attachmentLocation.AttachmentLocationViewModel
import com.naposystems.napoleonchat.ui.baseFragment.BaseViewModel
import com.naposystems.napoleonchat.ui.blockedContacts.BlockedContactsViewModel
import com.naposystems.napoleonchat.ui.cancelSubscription.CancelSubscriptionDialogViewModel
import com.naposystems.napoleonchat.ui.changeParams.ChangeParamsDialogViewModel
import com.naposystems.napoleonchat.ui.colorScheme.ColorSchemeViewModel
import com.naposystems.napoleonchat.ui.contactProfile.ContactProfileViewModel
import com.naposystems.napoleonchat.ui.contactUs.ContactUsViewModel
import com.naposystems.napoleonchat.ui.contacts.ContactsViewModel
import com.naposystems.napoleonchat.ui.conversation.ConversationViewModel
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallViewModel
import com.naposystems.napoleonchat.ui.editAccessPin.EditAccessPinViewModel
import com.naposystems.napoleonchat.ui.enterPin.EnterPinViewModel
import com.naposystems.napoleonchat.ui.home.HomeViewModel
import com.naposystems.napoleonchat.ui.languageSelection.LanguageSelectionViewModel
import com.naposystems.napoleonchat.ui.logout.LogoutDialogViewModel
import com.naposystems.napoleonchat.ui.mainActivity.MainActivityViewModel
import com.naposystems.napoleonchat.ui.multi.MultipleAttachmentViewModel
import com.naposystems.napoleonchat.ui.muteConversation.MuteConversationViewModel
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.NapoleonKeyboardGifViewModel
import com.naposystems.napoleonchat.ui.notificationSetting.NotificationSettingViewModel
import com.naposystems.napoleonchat.ui.previewBackgroundChat.PreviewBackgroundChatViewModel
import com.naposystems.napoleonchat.ui.previewImage.PreviewImageViewModel
import com.naposystems.napoleonchat.ui.previewMedia.PreviewMediaViewModel
import com.naposystems.napoleonchat.ui.profile.ProfileViewModel
import com.naposystems.napoleonchat.ui.recoveryAccount.RecoveryAccountViewModel
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.RecoveryAccountQuestionsViewModel
import com.naposystems.napoleonchat.ui.register.accessPin.AccessPinViewModel
import com.naposystems.napoleonchat.ui.register.enterCode.EnterCodeViewModel
import com.naposystems.napoleonchat.ui.register.sendCode.SendCodeViewModel
import com.naposystems.napoleonchat.ui.register.validateNickname.ValidateNicknameViewModel
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.RegisterRecoveryAccountViewModel
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionViewModel
import com.naposystems.napoleonchat.ui.securitySettings.SecuritySettingsViewModel
import com.naposystems.napoleonchat.ui.selfDestructTime.SelfDestructTimeViewModel
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentViewModel
import com.naposystems.napoleonchat.ui.splash.SplashViewModel
import com.naposystems.napoleonchat.ui.status.StatusViewModel
import com.naposystems.napoleonchat.ui.subscription.SubscriptionViewModel
import com.naposystems.napoleonchat.ui.timeAccessPin.TimeAccessPinDialogViewModel
import com.naposystems.napoleonchat.ui.unlockAppTime.UnlockAppTimeViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.camera.CameraShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.ContactRepositoryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.FriendShipActionShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utility.viewModel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SendCodeViewModel::class)
    internal abstract fun bindSendCodeViewModel(viewModel: SendCodeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EnterCodeViewModel::class)
    internal abstract fun bindEnterCodeViewModel(viewModel: EnterCodeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguageSelectionViewModel::class)
    internal abstract fun bindLanguageSelectionViewModel(viewModel: LanguageSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ValidateNicknameViewModel::class)
    internal abstract fun bindValidateNicknameViewModel(viewModel: ValidateNicknameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccessPinViewModel::class)
    internal abstract fun bindAccessPinViewModel(viewModel: AccessPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    internal abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    internal abstract fun bindProfileViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    internal abstract fun bindStatusViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlockedContactsViewModel::class)
    internal abstract fun bindBlockedContactsViewModel(viewModel: BlockedContactsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppearanceSettingsViewModel::class)
    internal abstract fun bindAppearanceSettingsViewModel(viewModel: AppearanceSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PreviewBackgroundChatViewModel::class)
    internal abstract fun bindPreviewBackgroundChatViewModel(viewModel: PreviewBackgroundChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PreviewImageViewModel::class)
    internal abstract fun bindPreviewImageViewModel(viewModel: PreviewImageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ColorSchemeViewModel::class)
    internal abstract fun bindColorSchemeViewModel(viewModel: ColorSchemeViewModel): ViewModel

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
    @ViewModelKey(SecuritySettingsViewModel::class)
    internal abstract fun bindSecuritySettingsViewModel(viewModel: SecuritySettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelfDestructTimeViewModel::class)
    internal abstract fun bindSelfDestructTimeViewModel(viewModel: SelfDestructTimeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditAccessPinViewModel::class)
    internal abstract fun bindEditAccessPinViewModel(viewModel: EditAccessPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeAccessPinDialogViewModel::class)
    internal abstract fun bindTimeAccessPinDialogViewModel(viewModel: TimeAccessPinDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel::class)
    internal abstract fun bindContactsViewModel(viewModel: ContactsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactUsViewModel::class)
    internal abstract fun bindContactUsViewModel(viewModel: ContactUsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel::class)
    internal abstract fun bindConversationViewModel(viewModel: ConversationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegisterRecoveryAccountViewModel::class)
    internal abstract fun bindRegisterRecoveryAccountViewModel(viewModel: RegisterRecoveryAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegisterRecoveryAccountQuestionViewModel::class)
    internal abstract fun bindRegisterRecoveryAccountQuestionViewModel(viewModel: RegisterRecoveryAccountQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecoveryAccountViewModel::class)
    internal abstract fun bindRecoveryAccountViewModel(viewModel: RecoveryAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecoveryAccountQuestionsViewModel::class)
    internal abstract fun bindRecoveryAccountQuestionsViewModel(viewModel: RecoveryAccountQuestionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddContactViewModel::class)
    internal abstract fun bindAddContactViewModel(viewModel: AddContactViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    internal abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivateBiometricsViewModel::class)
    internal abstract fun bindActivateBiometricsViewModel(viewModel: ActivateBiometricsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EnterPinViewModel::class)
    internal abstract fun bindEnterPinViewModel(viewModel: EnterPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UnlockAppTimeViewModel::class)
    internal abstract fun bindUnlockAppTimeViewModel(viewModel: UnlockAppTimeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BaseViewModel::class)
    internal abstract fun bindBaseViewModel(viewModel: BaseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactProfileViewModel::class)
    internal abstract fun bindContactProfileViewModel(viewModel: ContactProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MuteConversationViewModel::class)
    internal abstract fun bindMuteConversationViewModel(viewModel: MuteConversationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangeParamsDialogViewModel::class)
    internal abstract fun bindChangeFakesDialogViewModel(viewModel: ChangeParamsDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShareContactViewModel::class)
    internal abstract fun bindShareContactViewModel(viewModel: ShareContactViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelfDestructTimeMessageNotSentViewModel::class)
    internal abstract fun bindSelfDestructTimeMessageNotSentViewModel(viewModel: SelfDestructTimeMessageNotSentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttachmentAudioViewModel::class)
    internal abstract fun bindAttachmentAudioViewModel(viewModel: AttachmentAudioViewModel): ViewModel

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
    @ViewModelKey(AttachmentGalleryFoldersViewModel::class)
    internal abstract fun bindAttachmentGalleryFoldersViewModel(viewModel: AttachmentGalleryFoldersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttachmentGalleryViewModel::class)
    internal abstract fun bindAttachmentGalleryViewModel(viewModel: AttachmentGalleryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountAttackDialogViewModel::class)
    internal abstract fun bindAccountAttackDialogViewModel(viewModel: AccountAttackDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscriptionViewModel::class)
    internal abstract fun bindSubscriptionViewModel(viewModel: SubscriptionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NapoleonKeyboardGifViewModel::class)
    internal abstract fun bindNapoleonKeyboardGifViewModel(viewModel: NapoleonKeyboardGifViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttachmentLocationViewModel::class)
    internal abstract fun bindAttachmentLocationViewModel(viewModel: AttachmentLocationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationCallViewModel::class)
    internal abstract fun bindConversationCallViewModel(viewModel: ConversationCallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PreviewMediaViewModel::class)
    internal abstract fun bindPreviewMediaViewModel(viewModel: PreviewMediaViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogoutDialogViewModel::class)
    internal abstract fun bindLogoutDialogViewModel(viewModel: LogoutDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CancelSubscriptionDialogViewModel::class)
    internal abstract fun bindCancelSubscriptionDialogViewModel(viewModel: CancelSubscriptionDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendShipActionShareViewModel::class)
    internal abstract fun bindFriendShipActionShareViewModel(viewModel: FriendShipActionShareViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DefaultPreferencesViewModel::class)
    internal abstract fun bindDefaultPreferencesViewModel(viewModel: DefaultPreferencesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationSettingViewModel::class)
    internal abstract fun bindNotificationSettingViewModel(viewModel: NotificationSettingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MultipleAttachmentViewModel::class)
    internal abstract fun bindMultipleAttachmentViewModel(
        viewModel: MultipleAttachmentViewModel
    ): ViewModel
}