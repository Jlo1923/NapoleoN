package com.naposystems.napoleonchat.di.module.repository

import com.naposystems.napoleonchat.repository.accessPin.AccessPinRepository
import com.naposystems.napoleonchat.repository.accessPin.AccessPinRepositoryImp
import com.naposystems.napoleonchat.repository.addContact.AddContactRepositoryImp
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepositoryImp
import com.naposystems.napoleonchat.repository.attachmentGallery.AttachmentGalleryRepository
import com.naposystems.napoleonchat.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import com.naposystems.napoleonchat.repository.attachmentLocation.AttachmentLocationRepository
import com.naposystems.napoleonchat.repository.base.BaseRepository
import com.naposystems.napoleonchat.repository.base.BaseRepositoryImp
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepositoryImp
import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepository
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepository
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepositoryImp
import com.naposystems.napoleonchat.repository.contacts.ContactsRepository
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepository
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepositoryImp
import com.naposystems.napoleonchat.repository.conversationMute.ConversationMuteRepository
import com.naposystems.napoleonchat.repository.editAccessPin.EditAccessPinRepository
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepository
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepositoryImp
import com.naposystems.napoleonchat.repository.enterPin.EnterPinRepository
import com.naposystems.napoleonchat.repository.home.HomeRepository
import com.naposystems.napoleonchat.repository.home.HomeRepositoryImp
import com.naposystems.napoleonchat.repository.languageSelection.LanguageSelectionRepository
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepository
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepositoryImp
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.napoleonchat.repository.notificationSettings.NotificationSettingRepository
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepository
import com.naposystems.napoleonchat.repository.previewMedia.PreviewMediaRepository
import com.naposystems.napoleonchat.repository.profile.ProfileRepository
import com.naposystems.napoleonchat.repository.profile.ProfileRepositoryImp
import com.naposystems.napoleonchat.repository.recoveryAccount.RecoveryAccountRepository
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.napoleonchat.repository.securitySettings.SecuritySettingsRepository
import com.naposystems.napoleonchat.repository.selfDestructTime.SelfDestructTimeRepository
import com.naposystems.napoleonchat.repository.selfDestructTimeMessageNotSent.SelfDestructTimeMessageNotSentRepository
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepositoryImp
import com.naposystems.napoleonchat.repository.splash.SplashRepository
import com.naposystems.napoleonchat.repository.splash.SplashRepositoryImp
import com.naposystems.napoleonchat.repository.status.StatusRepository
import com.naposystems.napoleonchat.repository.status.StatusRepositoryImp
import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepository
import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepository
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepositoryImp
import com.naposystems.napoleonchat.service.download.contract.IContractDownloadService
import com.naposystems.napoleonchat.service.download.repository.DownloadServiceRepository
import com.naposystems.napoleonchat.service.multiattachment.contract.IContractMultipleUpload
import com.naposystems.napoleonchat.service.multiattachment.repository.MultipleUploadRepository
import com.naposystems.napoleonchat.service.uploadService.IContractUploadService
import com.naposystems.napoleonchat.service.uploadService.UploadServiceRepository
import com.naposystems.napoleonchat.repository.addContact.AddContactRepository
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepository
import com.naposystems.napoleonchat.ui.attachmentGallery.IContractAttachmentGallery
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import com.naposystems.napoleonchat.ui.attachmentLocation.IContractAttachmentLocation
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepository
import com.naposystems.napoleonchat.ui.colorScheme.IContractColorScheme
import com.naposystems.napoleonchat.ui.contactProfile.IContractContactProfile
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepository
import com.naposystems.napoleonchat.ui.contacts.IContractContacts
import com.naposystems.napoleonchat.ui.conversation.ConversationRepository
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.repository.MyMultiAttachmentMsgRepository
import com.naposystems.napoleonchat.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.napoleonchat.ui.enterPin.IContractEnterPin
import com.naposystems.napoleonchat.ui.languageSelection.IContractLanguageSelection
import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.repository.MultipleAttachmentRepository
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentItemPreview
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.multipreview.repository.MultipleAttachmentPreviewItemRepository
import com.naposystems.napoleonchat.ui.multipreview.repository.MultipleAttachmentPreviewRepository
import com.naposystems.napoleonchat.ui.muteConversation.IMuteConversation
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.IContractNapoleonKeyboardGif
import com.naposystems.napoleonchat.ui.notificationSetting.IContractNotificationSetting
import com.naposystems.napoleonchat.ui.previewBackgroundChat.IContractPreviewBackgroundChat
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.ui.recoveryAccount.IContractRecoveryAccount
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.napoleonchat.ui.securitySettings.IContractSecuritySettings
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.IContractSelfDestructTimeMessageNotSent
import com.naposystems.napoleonchat.ui.subscription.IContractSubscription
import com.naposystems.napoleonchat.ui.unlockAppTime.IContractUnlockAppTime
import com.naposystems.napoleonchat.webRTC.service.WebRTCServiceRepository
import com.naposystems.napoleonchat.webRTC.service.WebRTCServiceRepositoryImp
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindBaseRepository(repository: BaseRepositoryImp): BaseRepository

    @Binds
    abstract fun bindSplashRepository(repository: SplashRepositoryImp): SplashRepository

    @Binds
    abstract fun bindHomeRepository(repository: HomeRepositoryImp): HomeRepository

    @Binds
    abstract fun bindProfileRepository(repository: ProfileRepositoryImp): ProfileRepository

    @Binds
    abstract fun bindMainActivityRepository(repository: MainActivityRepositoryImp): MainActivityRepository

    @Binds
    abstract fun bindSendCodeRepository(repository: SendCodeRepositoryImp): SendCodeRepository

    @Binds
    abstract fun bindStatusRepository(repository: StatusRepositoryImp): StatusRepository

    @Binds
    abstract fun bindEnterCodeRepository(repository: EnterCodeRepositoryImp): EnterCodeRepository

    @Binds
    abstract fun bindValidateNicknameRepository(repository: ValidateNicknameRepositoryImp): ValidateNicknameRepository

    @Binds
    abstract fun bindAccessPinRepository(repository: AccessPinRepositoryImp): AccessPinRepository

    @Binds
    abstract fun bindAddContactRepository(repository: AddContactRepositoryImp): AddContactRepository

    @Binds
    abstract fun bindAppearanceSettingsRepository(repository: AppearanceSettingsRepositoryImp): AppearanceSettingsRepository

    @Binds
    abstract fun bindBlockedContactRepository(repository: BlockedContactRepositoryImp): BlockedContactRepository

    @Binds
    abstract fun bindContactUsRepository(repository: ContactUsRepositoryImp): ContactUsRepository

    //NO Refactorizados

    @Binds
    abstract fun bindAttachmentGalleryFolderRepository(repository: AttachmentGalleryFolderRepository): IContractAttachmentGalleryFolders.Repository

    @Binds
    abstract fun bindAttachmentGalleryRepository(repository: AttachmentGalleryRepository): IContractAttachmentGallery.Repository

    @Binds
    abstract fun bindAttachmentLocationRepository(repository: AttachmentLocationRepository): IContractAttachmentLocation.Repository

    @Binds
    abstract fun bindColorSchemeRepository(repository: ColorSchemeRepository): IContractColorScheme.Repository

    @Binds
    abstract fun bindContactProfileRepository(repository: ContactProfileRepository): IContractContactProfile.Repository

    @Binds
    abstract fun bindContactsRepository(repository: ContactsRepository): IContractContacts.Repository

    @Binds
    abstract fun bindConversationCallRepository(repository: ConversationCallRepositoryImp): ConversationCallRepository

    @Binds
    abstract fun bindConversationRepository(repository: ConversationRepository): IContractConversation.Repository

    @Binds
    abstract fun bindEditAccessPinRepository(repository: EditAccessPinRepository): IContractEditAccessPin.Repository

    @Binds
    abstract fun bindEnterPinRepository(repository: EnterPinRepository): IContractEnterPin.Repository

    @Binds
    abstract fun bindLanguageSelectionRepository(repository: LanguageSelectionRepository): IContractLanguageSelection.Repository


    @Binds
    abstract fun bindConversationMuteRepository(repository: ConversationMuteRepository): IMuteConversation.Repository

    @Binds
    abstract fun bindNapoleonKeyboardGifRepository(repository: NapoleonKeyboardGifRepository): IContractNapoleonKeyboardGif.Repository

    @Binds
    abstract fun bindNotificationSettingRepository(repository: NotificationSettingRepository): IContractNotificationSetting.Repository

    @Binds
    abstract fun bindPreviewBackgroundChatRepository(repository: PreviewBackgroundChatRepository): IContractPreviewBackgroundChat.Repository

    @Binds
    abstract fun bindPreviewMediaRepository(repository: PreviewMediaRepository): IContractPreviewMedia.Repository


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
    abstract fun bindSubscriptionRepository(repository: SubscriptionRepository): IContractSubscription.Repository

    @Binds
    abstract fun bindUnlockAppTimeRepository(repository: UnlockAppTimeRepository): IContractUnlockAppTime.Repository

    @Binds
    abstract fun bindWebRTCServiceRepository(repository: WebRTCServiceRepositoryImp): WebRTCServiceRepository

    @Binds
    abstract fun provideMultiUploadServiceRepository(repository: UploadServiceRepository): IContractUploadService.Repository

    @Binds
    abstract fun provideUploadServiceRepository(
        repository: MultipleUploadRepository
    ): IContractMultipleUpload.Repository

    @Binds
    abstract fun provideMultipleAttachmentRepository(repository: MultipleAttachmentRepository):
            IContractMultipleAttachment.Repository

    @Binds
    abstract fun provideMultipleAttachmentPreviewRepository(repository: MultipleAttachmentPreviewRepository):
            IContractMultipleAttachmentPreview.Repository

    @Binds
    abstract fun provideMyMultiAttachmentMsgRepository(
        repository: MyMultiAttachmentMsgRepository
    ): IContractMyMultiAttachmentMsg.Repository

    @Binds
    abstract fun provideDownloadServiceRepository(
        repository: DownloadServiceRepository
    ): IContractDownloadService.Repository

    @Binds
    abstract fun provideMultipleAttachmentPreviewItemRepository(
        repository: MultipleAttachmentPreviewItemRepository
    ): IContractMultipleAttachmentItemPreview.Repository

}