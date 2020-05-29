package com.naposystems.pepito.di

import com.naposystems.pepito.ui.accountAttack.AccountAttackDialogFragment
import com.naposystems.pepito.ui.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.pepito.ui.addContact.AddContactFragment
import com.naposystems.pepito.ui.appearanceSettings.AppearanceSettingsFragment
import com.naposystems.pepito.ui.attachmentAudio.AttachmentAudioFragment
import com.naposystems.pepito.ui.attachmentGallery.AttachmentGalleryFragment
import com.naposystems.pepito.ui.attachmentGalleryFolder.AttachmentGalleryFoldersFragment
import com.naposystems.pepito.ui.attachmentLocation.AttachmentLocationFragment
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.blockedContacts.BlockedContactsFragment
import com.naposystems.pepito.ui.changeParams.ChangeParamsDialogFragment
import com.naposystems.pepito.ui.colorScheme.ColorSchemeFragment
import com.naposystems.pepito.ui.contactProfile.ContactProfileFragment
import com.naposystems.pepito.ui.contactUs.ContactUsFragment
import com.naposystems.pepito.ui.contacts.ContactsFragment
import com.naposystems.pepito.ui.conversation.ConversationFragment
import com.naposystems.pepito.ui.conversationCamera.ConversationCameraFragment
import com.naposystems.pepito.ui.deletionDialog.DeletionMessagesDialogFragment
import com.naposystems.pepito.ui.editAccessPin.EditAccessPinFragment
import com.naposystems.pepito.ui.enterPin.EnterPinFragment
import com.naposystems.pepito.ui.home.HomeFragment
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.pepito.ui.previewBackgroundChat.PreviewBackgroundChatFragment
import com.naposystems.pepito.ui.previewImage.PreviewImageFragment
import com.naposystems.pepito.ui.previewMedia.PreviewMediaFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.ui.recoveryAccount.RecoveryAccountFragment
import com.naposystems.pepito.ui.recoveryAccountQuestions.RecoveryAccountQuestionsFragment
import com.naposystems.pepito.ui.recoveryOlderAccountQuestions.RecoveryOlderAccountQuestionsFragment
import com.naposystems.pepito.ui.register.accessPin.AccessPinFragment
import com.naposystems.pepito.ui.register.enterCode.EnterCodeFragment
import com.naposystems.pepito.ui.register.sendCode.SendCodeFragment
import com.naposystems.pepito.ui.register.validateNickname.ValidateNicknameFragment
import com.naposystems.pepito.ui.registerRecoveryAccount.RegisterRecoveryAccountFragment
import com.naposystems.pepito.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionFragment
import com.naposystems.pepito.ui.securitySettings.SecuritySettingsFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogFragment
import com.naposystems.pepito.ui.splash.SplashFragment
import com.naposystems.pepito.ui.status.StatusFragment
import com.naposystems.pepito.ui.subscription.SubscriptionFragment
import com.naposystems.pepito.ui.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.pepito.ui.timeFormat.TimeFormatDialogFragment
import com.naposystems.pepito.ui.unlockAppTime.UnlockAppTimeFragment
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {

    @ContributesAndroidInjector
    abstract fun bindSplashFragment(): SplashFragment

    @ContributesAndroidInjector
    abstract fun bindSendCodeFragment(): SendCodeFragment

    @ContributesAndroidInjector
    abstract fun bindEnterCodeFragment(): EnterCodeFragment

    @ContributesAndroidInjector
    abstract fun bindLanguageSelectionFragment(): LanguageSelectionDialogFragment

    @ContributesAndroidInjector
    abstract fun bindValidateNicknameFragment(): ValidateNicknameFragment

    @ContributesAndroidInjector
    abstract fun bindAccessPinFragment(): AccessPinFragment

    @ContributesAndroidInjector
    abstract fun bindProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun bindStatusFragment(): StatusFragment

    @ContributesAndroidInjector
    abstract fun bindBlockedContactsFragment(): BlockedContactsFragment

    @ContributesAndroidInjector
    abstract fun bindAppearanceSettingsFragment(): AppearanceSettingsFragment

    @ContributesAndroidInjector
    abstract fun bindPreviewBackgroundChatFragment(): PreviewBackgroundChatFragment

    @ContributesAndroidInjector
    abstract fun bindPreviewImageFragment(): PreviewImageFragment

    @ContributesAndroidInjector
    abstract fun bindColorSchemeFragment(): ColorSchemeFragment

    @ContributesAndroidInjector
    abstract fun bindUserDisplayFormatDialogFragment(): UserDisplayFormatDialogFragment

    @ContributesAndroidInjector
    abstract fun bindTimeFormatDialogFragment(): TimeFormatDialogFragment

    @ContributesAndroidInjector
    abstract fun bindSecuritySettingsFragment(): SecuritySettingsFragment

    @ContributesAndroidInjector
    abstract fun bindSelfDestructTimeDialogFragment(): SelfDestructTimeDialogFragment

    @ContributesAndroidInjector
    abstract fun bindEditAccessPinFragment(): EditAccessPinFragment

    @ContributesAndroidInjector
    abstract fun bindTimeAccessPinDialogFragment(): TimeAccessPinDialogFragment

    @ContributesAndroidInjector
    abstract fun bindContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    abstract fun bindContactUsFragment(): ContactUsFragment

    @ContributesAndroidInjector
    abstract fun bindConversationFragment(): ConversationFragment

    @ContributesAndroidInjector
    abstract fun bindRegisterRecoveryAccountFragment(): RegisterRecoveryAccountFragment

    @ContributesAndroidInjector
    abstract fun bindRegisterRecoveryAccountQuestionFragment(): RegisterRecoveryAccountQuestionFragment

    @ContributesAndroidInjector
    abstract fun bindRecoveryAccountFragment(): RecoveryAccountFragment

    @ContributesAndroidInjector
    abstract fun bindRecoveryAccountQuestionsFragment(): RecoveryAccountQuestionsFragment

    @ContributesAndroidInjector
    abstract fun bindAddContactFragment(): AddContactFragment

    @ContributesAndroidInjector
    abstract fun bindHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun bindActivateBiometricsFragment(): ActivateBiometricsDialogFragment

    @ContributesAndroidInjector
    abstract fun bindEnterPinFragment(): EnterPinFragment

    @ContributesAndroidInjector
    abstract fun binUnlockAppTimeFragment(): UnlockAppTimeFragment

    @ContributesAndroidInjector
    abstract fun binConversationCameraFragment(): ConversationCameraFragment

    @ContributesAndroidInjector
    abstract fun binBaseFragment(): BaseFragment

    @ContributesAndroidInjector
    abstract fun bindContactProfileFragment(): ContactProfileFragment

    @ContributesAndroidInjector
    abstract fun bindConversationMuteDialogFragment(): MuteConversationDialogFragment

    @ContributesAndroidInjector
    abstract fun bindChangeFakesDialogFragment(): ChangeParamsDialogFragment

    @ContributesAndroidInjector
    abstract fun bindSelfDestructTimeMessageNotSentDialogFragment(): SelfDestructTimeMessageNotSentDialogFragment

    @ContributesAndroidInjector
    abstract fun bindAttachmentAudioFragment(): AttachmentAudioFragment

    @ContributesAndroidInjector
    abstract fun bindAttachmentGalleryFoldersFragment(): AttachmentGalleryFoldersFragment

    @ContributesAndroidInjector
    abstract fun bindAttachmentGalleryFragment(): AttachmentGalleryFragment

    @ContributesAndroidInjector
    abstract fun bindValidatePasswordPreviousRecoveryAccountFragment(): ValidatePasswordPreviousRecoveryAccountFragment

    @ContributesAndroidInjector
    abstract fun bindRecoveryOlderAccountQuestionsFragment(): RecoveryOlderAccountQuestionsFragment

    @ContributesAndroidInjector
    abstract fun bindAccountAttackDialogFragment(): AccountAttackDialogFragment

    @ContributesAndroidInjector
    abstract fun bindSubscriptionFragment(): SubscriptionFragment

    @ContributesAndroidInjector
    abstract fun bindDeletionMessagesDialogFragment(): DeletionMessagesDialogFragment

    @ContributesAndroidInjector
    abstract fun bindNapoleonKeyboardGifFragment(): NapoleonKeyboardGifFragment

    @ContributesAndroidInjector
    abstract fun bindAttachmentLocationFragment(): AttachmentLocationFragment

    @ContributesAndroidInjector
    abstract fun bindPreviewMediaFragment(): PreviewMediaFragment
}