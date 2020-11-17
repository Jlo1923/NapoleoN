package com.naposystems.napoleonchat.di

import com.naposystems.napoleonchat.ui.accountAttack.AccountAttackDialogFragment
import com.naposystems.napoleonchat.ui.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.napoleonchat.ui.addContact.AddContactFragment
import com.naposystems.napoleonchat.ui.appearanceSettings.AppearanceSettingsFragment
import com.naposystems.napoleonchat.ui.attachmentAudio.AttachmentAudioFragment
import com.naposystems.napoleonchat.ui.attachmentGallery.AttachmentGalleryFragment
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.AttachmentGalleryFoldersFragment
import com.naposystems.napoleonchat.ui.attachmentLocation.AttachmentLocationFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.blockedContacts.BlockedContactsFragment
import com.naposystems.napoleonchat.ui.cancelSubscription.CancelSubscriptionDialogFragment
import com.naposystems.napoleonchat.ui.changeParams.ChangeFakeParamsDialogFragment
import com.naposystems.napoleonchat.ui.changeParams.ChangeParamsDialogFragment
import com.naposystems.napoleonchat.ui.colorScheme.ColorSchemeFragment
import com.naposystems.napoleonchat.ui.contactProfile.ContactProfileFragment
import com.naposystems.napoleonchat.ui.contactUs.ContactUsFragment
import com.naposystems.napoleonchat.ui.contacts.ContactsFragment
import com.naposystems.napoleonchat.ui.conversation.ConversationFragment
import com.naposystems.napoleonchat.ui.conversationCamera.ConversationCameraFragment
import com.naposystems.napoleonchat.ui.deletionDialog.DeletionMessagesDialogFragment
import com.naposystems.napoleonchat.ui.editAccessPin.EditAccessPinFragment
import com.naposystems.napoleonchat.ui.enterPin.EnterPinFragment
import com.naposystems.napoleonchat.ui.home.HomeFragment
import com.naposystems.napoleonchat.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.napoleonchat.ui.logout.LogoutDialogFragment
import com.naposystems.napoleonchat.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.napoleonchat.ui.previewBackgroundChat.PreviewBackgroundChatFragment
import com.naposystems.napoleonchat.ui.previewImage.PreviewImageFragment
import com.naposystems.napoleonchat.ui.previewMedia.PreviewMediaFragment
import com.naposystems.napoleonchat.ui.profile.ProfileFragment
import com.naposystems.napoleonchat.ui.recoveryAccount.RecoveryAccountFragment
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.RecoveryAccountQuestionsFragment
import com.naposystems.napoleonchat.ui.recoveryOlderAccountQuestions.RecoveryOlderAccountQuestionsFragment
import com.naposystems.napoleonchat.ui.register.accessPin.AccessPinFragment
import com.naposystems.napoleonchat.ui.register.enterCode.EnterCodeFragment
import com.naposystems.napoleonchat.ui.register.sendCode.SendCodeFragment
import com.naposystems.napoleonchat.ui.register.validateNickname.ValidateNicknameFragment
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.RegisterRecoveryAccountFragment
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionFragment
import com.naposystems.napoleonchat.ui.securitySettings.SecuritySettingsFragment
import com.naposystems.napoleonchat.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogFragment
import com.naposystems.napoleonchat.ui.splash.SplashFragment
import com.naposystems.napoleonchat.ui.status.StatusFragment
import com.naposystems.napoleonchat.ui.subscription.SubscriptionFragment
import com.naposystems.napoleonchat.ui.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.napoleonchat.ui.timeFormat.TimeFormatDialogFragment
import com.naposystems.napoleonchat.ui.unlockAppTime.UnlockAppTimeFragment
import com.naposystems.napoleonchat.ui.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.napoleonchat.ui.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountFragment
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
    abstract fun bindChangeFakeParamsDialogFragment(): ChangeFakeParamsDialogFragment

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

    @ContributesAndroidInjector
    abstract fun bindLogoutDialogFragment(): LogoutDialogFragment

    @ContributesAndroidInjector
    abstract fun bindCancelSubscriptionDialogFragment(): CancelSubscriptionDialogFragment
}