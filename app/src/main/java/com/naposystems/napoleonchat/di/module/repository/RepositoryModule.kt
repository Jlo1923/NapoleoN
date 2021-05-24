package com.naposystems.napoleonchat.di.module.repository

import com.naposystems.napoleonchat.repository.accessPin.AccessPinRepository
import com.naposystems.napoleonchat.repository.accessPin.AccessPinRepositoryImp
import com.naposystems.napoleonchat.repository.addContact.AddContactRepository
import com.naposystems.napoleonchat.repository.addContact.AddContactRepositoryImp
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepository
import com.naposystems.napoleonchat.repository.appearanceSettings.AppearanceSettingsRepositoryImp
import com.naposystems.napoleonchat.repository.attachmentGallery.AttachmentGalleryRepository
import com.naposystems.napoleonchat.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import com.naposystems.napoleonchat.repository.attachmentLocation.AttachmentLocationRepository
import com.naposystems.napoleonchat.repository.base.BaseRepository
import com.naposystems.napoleonchat.repository.base.BaseRepositoryImp
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepository
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepositoryImp
import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepository
import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepositoryImp
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepository
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepositoryImp
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepository
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepositoryImp
import com.naposystems.napoleonchat.repository.contacts.ContactsRepositoryImp
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepository
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepositoryImp
import com.naposystems.napoleonchat.repository.editAccessPin.EditAccessPinRepository
import com.naposystems.napoleonchat.repository.editAccessPin.EditAccessPinRepositoryImp
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepository
import com.naposystems.napoleonchat.repository.enterCode.EnterCodeRepositoryImp
import com.naposystems.napoleonchat.repository.enterPin.EnterPinRepositoryImp
import com.naposystems.napoleonchat.repository.home.HomeRepository
import com.naposystems.napoleonchat.repository.home.HomeRepositoryImp
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepository
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepositoryImp
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepositoryImp
import com.naposystems.napoleonchat.repository.notificationSettings.NotificationSettingRepository
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepositoryImp
import com.naposystems.napoleonchat.repository.previewMedia.PreviewMediaRepository
import com.naposystems.napoleonchat.repository.profile.ProfileRepository
import com.naposystems.napoleonchat.repository.profile.ProfileRepositoryImp
import com.naposystems.napoleonchat.repository.recoveryAccount.RecoveryAccountRepositoryImp
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepositoryImp
import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepositoryImp
import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepositoryImp
import com.naposystems.napoleonchat.repository.securitySettings.SecuritySettingsRepositoryImp
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepository
import com.naposystems.napoleonchat.repository.sendCode.SendCodeRepositoryImp
import com.naposystems.napoleonchat.repository.splash.SplashRepository
import com.naposystems.napoleonchat.repository.splash.SplashRepositoryImp
import com.naposystems.napoleonchat.repository.status.StatusRepository
import com.naposystems.napoleonchat.repository.status.StatusRepositoryImp
import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepositoryImp
import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepositoryImp
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepository
import com.naposystems.napoleonchat.repository.validateNickname.ValidateNicknameRepositoryImp
import com.naposystems.napoleonchat.service.download.contract.IContractDownloadService
import com.naposystems.napoleonchat.service.download.repository.DownloadServiceRepository
import com.naposystems.napoleonchat.service.multiattachment.contract.IContractMultipleUpload
import com.naposystems.napoleonchat.service.multiattachment.repository.MultipleUploadRepository
import com.naposystems.napoleonchat.service.uploadService.IContractUploadService
import com.naposystems.napoleonchat.service.uploadService.UploadServiceRepository
import com.naposystems.napoleonchat.ui.attachmentGallery.IContractAttachmentGallery
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import com.naposystems.napoleonchat.ui.attachmentLocation.IContractAttachmentLocation
import com.naposystems.napoleonchat.repository.contacts.ContactsRepository
import com.naposystems.napoleonchat.ui.conversation.ConversationRepository
import com.naposystems.napoleonchat.ui.conversation.IContractConversation
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.repository.MyMultiAttachmentMsgRepository
import com.naposystems.napoleonchat.repository.enterPin.EnterPinRepository
import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.repository.MultipleAttachmentRepository
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentItemPreview
import com.naposystems.napoleonchat.ui.multipreview.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.multipreview.repository.MultipleAttachmentPreviewItemRepository
import com.naposystems.napoleonchat.ui.multipreview.repository.MultipleAttachmentPreviewRepository
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.napoleonchat.ui.notificationSetting.IContractNotificationSetting
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepository
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.repository.recoveryAccount.RecoveryAccountRepository
import com.naposystems.napoleonchat.repository.recoveryAccountQuestions.RecoveryAccountQuestionsRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccount.RegisterRecoveryAccountRepository
import com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionRepository
import com.naposystems.napoleonchat.repository.securitySettings.SecuritySettingsRepository
import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepository
import com.naposystems.napoleonchat.repository.unlockAppTime.UnlockAppTimeRepository
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

    @Binds
    abstract fun bindEditAccessPinRepository(repository: EditAccessPinRepositoryImp): EditAccessPinRepository

    @Binds
    abstract fun bindColorSchemeRepository(repository: ColorSchemeRepositoryImp): ColorSchemeRepository

    @Binds
    abstract fun bindContactProfileRepository(repository: ContactProfileRepositoryImp): ContactProfileRepository

    @Binds
    abstract fun bindContactsRepository(repository: ContactsRepositoryImp): ContactsRepository

    @Binds
    abstract fun bindEnterPinRepository(repository: EnterPinRepositoryImp): EnterPinRepository

    @Binds
    abstract fun bindConversationCallRepository(repository: ConversationCallRepositoryImp): ConversationCallRepository

    @Binds
    abstract fun bindWebRTCServiceRepository(repository: WebRTCServiceRepositoryImp): WebRTCServiceRepository

    @Binds
    abstract fun bindPreviewBackgroundChatRepository(repository: PreviewBackgroundChatRepositoryImp): PreviewBackgroundChatRepository

    @Binds
    abstract fun bindRecoveryAccountRepository(repository: RecoveryAccountRepositoryImp): RecoveryAccountRepository

    @Binds
    abstract fun bindRecoveryAccountQuestionsRepository(repository: RecoveryAccountQuestionsRepositoryImp): RecoveryAccountQuestionsRepository

    @Binds
    abstract fun bindRegisterRecoveryAccountRepository(repository: RegisterRecoveryAccountRepositoryImp): RegisterRecoveryAccountRepository

    @Binds
    abstract fun bindRegisterRecoveryAccountQuestionRepository(repository: RegisterRecoveryAccountQuestionRepositoryImp): RegisterRecoveryAccountQuestionRepository

    @Binds
    abstract fun bindSecuritySettingsRepository(repository: SecuritySettingsRepositoryImp): SecuritySettingsRepository

    @Binds
    abstract fun bindSubscriptionRepository(repository: SubscriptionRepositoryImp): SubscriptionRepository

    @Binds
    abstract fun bindUnlockAppTimeRepository(repository: UnlockAppTimeRepositoryImp): UnlockAppTimeRepository

    @Binds
    abstract fun bindNapoleonKeyboardGifRepository(repository: NapoleonKeyboardGifRepositoryImp): NapoleonKeyboardGifRepository

    //NO Refactorizados

    @Binds
    abstract fun bindAttachmentGalleryFolderRepository(repository: AttachmentGalleryFolderRepository): IContractAttachmentGalleryFolders.Repository

    @Binds
    abstract fun bindAttachmentGalleryRepository(repository: AttachmentGalleryRepository): IContractAttachmentGallery.Repository

    @Binds
    abstract fun bindAttachmentLocationRepository(repository: AttachmentLocationRepository): IContractAttachmentLocation.Repository

    @Binds
    abstract fun bindConversationRepository(repository: ConversationRepository): IContractConversation.Repository

    @Binds
    abstract fun bindNotificationSettingRepository(repository: NotificationSettingRepository): IContractNotificationSetting.Repository

    @Binds
    abstract fun bindPreviewMediaRepository(repository: PreviewMediaRepository): IContractPreviewMedia.Repository

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