package com.naposystems.napoleonchat.di.module.sections

import com.naposystems.napoleonchat.repository.accountAttackDialog.AccountAttackDialogRepository
import com.naposystems.napoleonchat.repository.activateBiometrics.ActivateBiometricsRepository
import com.naposystems.napoleonchat.repository.addContact.AddContactRepository
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepository
import com.naposystems.napoleonchat.repository.attachmentGallery.AttachmentGalleryRepository
import com.naposystems.napoleonchat.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import com.naposystems.napoleonchat.repository.attachmentLocation.AttachmentLocationRepository
import com.naposystems.napoleonchat.repository.base.BaseRepository
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepository
import com.naposystems.napoleonchat.repository.cancelSubscription.CancelSubscriptionRepository
import com.naposystems.napoleonchat.repository.changeFakes.ChangeParamsDialogRepository
import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepository
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepository
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepository
import com.naposystems.napoleonchat.repository.contacts.ContactsRepository
import com.naposystems.napoleonchat.repository.conversation.ConversationRepository
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepository
import com.naposystems.napoleonchat.repository.conversationMute.ConversationMuteRepository
import com.naposystems.napoleonchat.repository.defaultPreferences.DefaultPreferencesRepository
import com.naposystems.napoleonchat.repository.editAccessPin.EditAccessPinRepository
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepository
import com.naposystems.napoleonchat.repository.enterPin.EnterPinRepository
import com.naposystems.napoleonchat.repository.home.HomeRepository
import com.naposystems.napoleonchat.repository.languageSelection.LanguageSelectionRepository
import com.naposystems.napoleonchat.repository.logout.LogoutRepository
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepository
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.napoleonchat.repository.notificationSettings.NotificationSettingRepository
import com.naposystems.napoleonchat.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepository
import com.naposystems.napoleonchat.repository.previewMedia.PreviewMediaRepository
import com.naposystems.napoleonchat.repository.profile.ProfileRepository
import com.naposystems.napoleonchat.repository.recoveryAccount.RecoveryAccountRepository
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.napoleonchat.repository.securitySettings.SecuritySettingsRepository
import com.naposystems.napoleonchat.repository.selfDestructTime.SelfDestructTimeRepository
import com.naposystems.napoleonchat.repository.selfDestructTimeMessageNotSent.SelfDestructTimeMessageNotSentRepository
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import com.naposystems.napoleonchat.repository.socket.SocketRepository
import com.naposystems.napoleonchat.repository.splash.SplashRepository
import com.naposystems.napoleonchat.repository.status.StatusRepository
import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepository
import com.naposystems.napoleonchat.repository.timeAccessPin.TimeAccessPinRepository
import com.naposystems.napoleonchat.repository.timeFormat.TimeFormatRepository
import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepository
import com.naposystems.napoleonchat.repository.uploadService.UploadServiceRepository
import com.naposystems.napoleonchat.repository.userDisplayFormat.UserDisplayFormatRepository
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.napoleonchat.repository.webRTCCallService.WebRTCCallServiceRepository
import com.naposystems.napoleonchat.service.uploadService.IContractUploadService
import com.naposystems.napoleonchat.service.webRTCCall.IContractWebRTCCallService
import com.naposystems.napoleonchat.ui.accountAttack.IContractAccountAttackDialog
import com.naposystems.napoleonchat.ui.activateBiometrics.IContractActivateBiometrics
import com.naposystems.napoleonchat.ui.addContact.IContractAddContact
import com.naposystems.napoleonchat.ui.appearanceSettings.IContractAppearanceSettings
import com.naposystems.napoleonchat.ui.attachmentGallery.IContractAttachmentGallery
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import com.naposystems.napoleonchat.ui.attachmentLocation.IContractAttachmentLocation
import com.naposystems.napoleonchat.ui.baseFragment.IContractBase
import com.naposystems.napoleonchat.ui.blockedContacts.IContractBlockedContact
import com.naposystems.napoleonchat.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.napoleonchat.ui.changeParams.IContractChangeDialogParams
import com.naposystems.napoleonchat.ui.colorScheme.IContractColorScheme
import com.naposystems.napoleonchat.ui.contactProfile.IContractContactProfile
import com.naposystems.napoleonchat.ui.contactUs.IContractContactUs
import com.naposystems.napoleonchat.ui.contacts.IContractContacts
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.ui.conversationCall.IContractConversationCall
import com.naposystems.napoleonchat.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.napoleonchat.ui.enterPin.IContractEnterPin
import com.naposystems.napoleonchat.ui.home.IContractHome
import com.naposystems.napoleonchat.ui.languageSelection.IContractLanguageSelection
import com.naposystems.napoleonchat.ui.logout.IContractLogout
import com.naposystems.napoleonchat.ui.mainActivity.IContractMainActivity
import com.naposystems.napoleonchat.ui.muteConversation.IMuteConversation
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.napoleonchat.ui.notificationSetting.IContractNotificationSetting
import com.naposystems.napoleonchat.ui.previewBackgroundChat.IContractPreviewBackgroundChat
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.ui.profile.IContractProfile
import com.naposystems.napoleonchat.ui.recoveryAccount.IContractRecoveryAccount
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.napoleonchat.ui.register.enterCode.IContractEnterCode
import com.naposystems.napoleonchat.ui.register.sendCode.IContractSendCode
import com.naposystems.napoleonchat.ui.register.validateNickname.IContractValidateNickname
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.napoleonchat.ui.securitySettings.IContractSecuritySettings
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.IContractSelfDestructTimeMessageNotSent
import com.naposystems.napoleonchat.ui.splash.IContractSplash
import com.naposystems.napoleonchat.ui.status.IContractStatus
import com.naposystems.napoleonchat.ui.subscription.IContractSubscription
import com.naposystems.napoleonchat.ui.timeAccessPin.IContractTimeAccessPin
import com.naposystems.napoleonchat.ui.timeFormat.IContractTimeFormat
import com.naposystems.napoleonchat.ui.unlockAppTime.IContractUnlockAppTime
import com.naposystems.napoleonchat.ui.userDisplayFormat.IContractUserDisplayFormat
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.IContractDefaultPreferences
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindBaseRepository(repository: BaseRepository): IContractBase.Repository

    @Binds
    abstract fun bindAccountAttackDialogRepository(repository: AccountAttackDialogRepository): IContractAccountAttackDialog.Repository

    @Binds
    abstract fun bindActivateBiometricsRepository(repository: ActivateBiometricsRepository): IContractActivateBiometrics.Repository

    @Binds
    abstract fun bindAddContactRepository(repository: AddContactRepository): IContractAddContact.Repository

    @Binds
    abstract fun bindAppearanceSettingsRepository(repository: AppearanceSettingsRepository): IContractAppearanceSettings.Repository

    @Binds
    abstract fun bindAttachmentGalleryFolderRepository(repository: AttachmentGalleryFolderRepository): IContractAttachmentGalleryFolders.Repository

    @Binds
    abstract fun bindAttachmentGalleryRepository(repository: AttachmentGalleryRepository): IContractAttachmentGallery.Repository

    @Binds
    abstract fun bindAttachmentLocationRepository(repository: AttachmentLocationRepository): IContractAttachmentLocation.Repository

    @Binds
    abstract fun bindBlockedContactRepository(repository: BlockedContactRepository): IContractBlockedContact.Repository

    @Binds
    abstract fun bindCancelSubscriptionRepository(repository: CancelSubscriptionRepository): IContractCancelSubscription.Repository

    @Binds
    abstract fun bindChangeParamsDialogRepository(repository: ChangeParamsDialogRepository): IContractChangeDialogParams.Repository

    @Binds
    abstract fun bindColorSchemeRepository(repository: ColorSchemeRepository): IContractColorScheme.Repository

    @Binds
    abstract fun bindContactProfileRepository(repository: ContactProfileRepository): IContractContactProfile.Repository

    @Binds
    abstract fun bindContactsRepository(repository: ContactsRepository): IContractContacts.Repository

    @Binds
    abstract fun bindContactUsRepository(repository: ContactUsRepository): IContractContactUs.Repository

    @Binds
    abstract fun bindConversationCallRepository(repository: ConversationCallRepository): IContractConversationCall.Repository

    @Binds
    abstract fun bindConversationRepository(repository: ConversationRepository): IContractConversation.Repository

    //CreateAccountRepository

    @Binds
    abstract fun bindDefaultPreferencesRepository(repository: DefaultPreferencesRepository): IContractDefaultPreferences.Repository

    @Binds
    abstract fun bindEditAccessPinRepository(repository: EditAccessPinRepository): IContractEditAccessPin.Repository

    @Binds
    abstract fun bindEnterCodeRepository(repository: EnterCodeRepository): IContractEnterCode.Repository

    @Binds
    abstract fun bindEnterPinRepository(repository: EnterPinRepository): IContractEnterPin.Repository

    @Binds
    abstract fun bindHomeRepository(repository: HomeRepository): IContractHome.Repository

    @Binds
    abstract fun bindLanguageSelectionRepository(repository: LanguageSelectionRepository): IContractLanguageSelection.Repository

    @Binds
    abstract fun bindLogoutRepository(repository: LogoutRepository): IContractLogout.Repository

    @Binds
    abstract fun bindMainActivityRepository(repository: MainActivityRepository): IContractMainActivity.Repository

    @Binds
    abstract fun bindConversationMuteRepository(repository: ConversationMuteRepository): IMuteConversation.Repository

    @Binds
    abstract fun bindNapoleonKeyboardGifRepository(repository: NapoleonKeyboardGifRepository): IContractNapoleonKeyboardGif.Repository

    @Binds
    abstract fun bindNotificationSettingRepository(repository: NotificationSettingRepository): IContractNotificationSetting.Repository

    @Binds
    abstract fun bindNotificationUtilsRepository(repository: NotificationUtilsRepository): IContractNotificationUtils.Repository

    @Binds
    abstract fun bindPreviewBackgroundChatRepository(repository: PreviewBackgroundChatRepository): IContractPreviewBackgroundChat.Repository

    @Binds
    abstract fun bindPreviewMediaRepository(repository: PreviewMediaRepository): IContractPreviewMedia.Repository

    @Binds
    abstract fun bindProfileRepository(repository: ProfileRepository): IContractProfile.Repository

    @Binds
    abstract fun bindRecoveryAccountRepository(repository: RecoveryAccountRepository): IContractRecoveryAccount.Repository

    @Binds
    abstract fun bindRecoveryAccountQuestionsRepository(repository: RecoveryAccountQuestionsRepository): IContractRecoveryAccountQuestions.Repository

    @Binds
    abstract fun bindRegisterRecoveryAccountRepository(repository: RegisterRecoveryAccountRepository): IContractRegisterRecoveryAccount.Repository

    @Binds
    abstract fun bindRegisterRecoveryAccountQuestionRepository(repository: RegisterRecoveryAccountQuestionRepository): IContractRegisterRecoveryAccountQuestion.Repository

    @Binds
    abstract fun bindSecuritySettingsRepository(repository: SecuritySettingsRepository): IContractSecuritySettings.Repository

    @Binds
    abstract fun bindSelfDestructTimeRepository(repository: SelfDestructTimeRepository): IContractSelfDestructTime.Repository

    @Binds
    abstract fun bindSelfDestructTimeMessageNotSentRepository(repository: SelfDestructTimeMessageNotSentRepository): IContractSelfDestructTimeMessageNotSent.Repository

    @Binds
    abstract fun bindSendCodeRepository(repository: SendCodeRepository): IContractSendCode.Repository

    @Binds
    abstract fun bindSocketRepository(repository: SocketRepository): IContractSocketService.Repository

    @Binds
    abstract fun bindSplashRepository(repository: SplashRepository): IContractSplash.Repository

    @Binds
    abstract fun bindStatusRepository(repository: StatusRepository): IContractStatus.Repository

    @Binds
    abstract fun bindSubscriptionRepository(repository: SubscriptionRepository): IContractSubscription.Repository

    @Binds
    abstract fun bindTimeAccessPinRepository(repository: TimeAccessPinRepository): IContractTimeAccessPin.Repository

    @Binds
    abstract fun bindTimeFormatRepository(repository: TimeFormatRepository): IContractTimeFormat.Repository

    @Binds
    abstract fun bindUnlockAppTimeRepository(repository: UnlockAppTimeRepository): IContractUnlockAppTime.Repository

    @Binds
    abstract fun bindUserDisplayFormatRepository(repository: UserDisplayFormatRepository): IContractUserDisplayFormat.Repository

    @Binds
    abstract fun bindValidateNicknameRepository(repository: ValidateNicknameRepository): IContractValidateNickname.Repository

    @Binds
    abstract fun bindWebRTCCallServiceRepository(repository: WebRTCCallServiceRepository): IContractWebRTCCallService.Repository

    @Binds
    abstract fun provideUploadServiceRepository(repository: UploadServiceRepository): IContractUploadService.Repository

}