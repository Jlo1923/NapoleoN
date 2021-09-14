package com.naposystems.napoleonchat.di.module.ui

import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogFragment
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogFragment
import com.naposystems.napoleonchat.dialog.changeParams.ChangeFakeParamsDialogFragment
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogFragment
import com.naposystems.napoleonchat.dialog.deletionMesssages.DeletionMessagesDialogFragment
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogFragment
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogFragment
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.napoleonchat.ui.addContact.AddContactFragment
import com.naposystems.napoleonchat.ui.appearanceSettings.AppearanceSettingsFragment
import com.naposystems.napoleonchat.ui.attachmentAudio.AttachmentAudioFragment
import com.naposystems.napoleonchat.ui.attachmentGallery.AttachmentGalleryFragment
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.AttachmentGalleryFoldersFragment
import com.naposystems.napoleonchat.ui.attachmentLocation.AttachmentLocationFragment
import com.naposystems.napoleonchat.ui.attachmentPreview.AttachmentPreviewFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.blockedContacts.BlockedContactsFragment
import com.naposystems.napoleonchat.ui.colorScheme.ColorSchemeFragment
import com.naposystems.napoleonchat.ui.contactProfile.ContactProfileFragment
import com.naposystems.napoleonchat.ui.contactUs.ContactUsFragment
import com.naposystems.napoleonchat.ui.contacts.ContactsFragment
import com.naposystems.napoleonchat.ui.conversation.ConversationFragment
import com.naposystems.napoleonchat.ui.conversationCamera.ConversationCameraFragment
import com.naposystems.napoleonchat.ui.customUserNotification.CustomUserNotificationFragment
import com.naposystems.napoleonchat.ui.editAccessPin.EditAccessPinFragment
import com.naposystems.napoleonchat.ui.enterPin.EnterPinFragment
import com.naposystems.napoleonchat.ui.help.HelpFragment
import com.naposystems.napoleonchat.ui.home.HomeFragment
import com.naposystems.napoleonchat.dialog.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.napoleonchat.ui.multipreview.fragments.MultipleAttachmentPreviewImageFragment
import com.naposystems.napoleonchat.ui.multipreview.fragments.MultipleAttachmentPreviewVideoFragment
import com.naposystems.napoleonchat.dialog.muteConversation.MuteConversationDialogFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.napoleonchat.ui.notificationSetting.NotificationSettingFragment
import com.naposystems.napoleonchat.ui.previewBackgroundChat.PreviewBackgroundChatFragment
import com.naposystems.napoleonchat.ui.previewImage.PreviewImageFragment
import com.naposystems.napoleonchat.ui.previewMedia.PreviewMediaFragment
import com.naposystems.napoleonchat.ui.profile.ProfileFragment
import com.naposystems.napoleonchat.ui.recoveryAccount.RecoveryAccountFragment
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.RecoveryAccountQuestionsFragment
import com.naposystems.napoleonchat.ui.register.accessPin.AccessPinFragment
import com.naposystems.napoleonchat.ui.register.enterCode.EnterCodeFragment
import com.naposystems.napoleonchat.ui.register.sendCode.SendCodeFragment
import com.naposystems.napoleonchat.ui.register.validateNickname.ValidateNicknameFragment
import com.naposystems.napoleonchat.ui.registerRecoveryAccount.RegisterRecoveryAccountFragment
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionFragment
import com.naposystems.napoleonchat.ui.securitySettings.SecuritySettingsFragment
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.ui.home.TabsPagerFragment
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogFragment
import com.naposystems.napoleonchat.ui.splash.SplashFragment
import com.naposystems.napoleonchat.ui.status.StatusFragment
import com.naposystems.napoleonchat.ui.subscription.SubscriptionFragment
import com.naposystems.napoleonchat.ui.unlockAppTime.UnlockAppTimeFragment
import com.naposystems.napoleonchat.utility.dialog.PermissionDialogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributesSplashFragment(): SplashFragment

    @ContributesAndroidInjector
    abstract fun contributeSendCodeFragment(): SendCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeEnterCodeFragment(): EnterCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeLanguageSelectionFragment(): LanguageSelectionDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeValidateNicknameFragment(): ValidateNicknameFragment

    @ContributesAndroidInjector
    abstract fun contributeAccessPinFragment(): AccessPinFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeStatusFragment(): StatusFragment

    @ContributesAndroidInjector
    abstract fun contributeBlockedContactsFragment(): BlockedContactsFragment

    @ContributesAndroidInjector
    abstract fun contributeAppearanceSettingsFragment(): AppearanceSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributePreviewBackgroundChatFragment(): PreviewBackgroundChatFragment

    @ContributesAndroidInjector
    abstract fun contributePreviewImageFragment(): PreviewImageFragment

    @ContributesAndroidInjector
    abstract fun contributeColorSchemeFragment(): ColorSchemeFragment

    @ContributesAndroidInjector
    abstract fun contributeUserDisplayFormatDialogFragment(): UserDisplayFormatDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeTimeFormatDialogFragment(): TimeFormatDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSecuritySettingsFragment(): SecuritySettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeSelfDestructTimeDialogFragment(): SelfDestructTimeDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeEditAccessPinFragment(): EditAccessPinFragment

    @ContributesAndroidInjector
    abstract fun contributeTimeAccessPinDialogFragment(): TimeAccessPinDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    abstract fun contributeContactUsFragment(): ContactUsFragment

    @ContributesAndroidInjector
    abstract fun contributeConversationFragment(): ConversationFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterRecoveryAccountFragment(): RegisterRecoveryAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterRecoveryAccountQuestionFragment(): RegisterRecoveryAccountQuestionFragment

    @ContributesAndroidInjector
    abstract fun contributeRecoveryAccountFragment(): RecoveryAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeRecoveryAccountQuestionsFragment(): RecoveryAccountQuestionsFragment

    @ContributesAndroidInjector
    abstract fun contributeAddContactFragment(): AddContactFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeActivateBiometricsFragment(): ActivateBiometricsDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeEnterPinFragment(): EnterPinFragment

    @ContributesAndroidInjector
    abstract fun contributeUnlockAppTimeFragment(): UnlockAppTimeFragment

    @ContributesAndroidInjector
    abstract fun contributeConversationCameraFragment(): ConversationCameraFragment

    @ContributesAndroidInjector
    abstract fun contributeBaseFragment(): BaseFragment

    @ContributesAndroidInjector
    abstract fun contributeContactProfileFragment(): ContactProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeConversationMuteDialogFragment(): MuteConversationDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeChangeFakesDialogFragment(): ChangeParamsDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeChangeFakeParamsDialogFragment(): ChangeFakeParamsDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSelfDestructTimeMessageNotSentDialogFragment(): SelfDestructTimeMessageNotSentDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeAttachmentAudioFragment(): AttachmentAudioFragment

    @ContributesAndroidInjector
    abstract fun contributeAttachmentGalleryFoldersFragment(): AttachmentGalleryFoldersFragment

    @ContributesAndroidInjector
    abstract fun contributeAttachmentGalleryFragment(): AttachmentGalleryFragment

    @ContributesAndroidInjector
    abstract fun contributeAccountAttackDialogFragment(): AccountAttackDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSubscriptionFragment(): SubscriptionFragment

    @ContributesAndroidInjector
    abstract fun contributeDeletionMessagesDialogFragment(): DeletionMessagesDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeNapoleonKeyboardGifFragment(): NapoleonKeyboardGifFragment

    @ContributesAndroidInjector
    abstract fun contributeAttachmentLocationFragment(): AttachmentLocationFragment

    @ContributesAndroidInjector
    abstract fun contributePreviewMediaFragment(): PreviewMediaFragment

    @ContributesAndroidInjector
    abstract fun contributeLogoutDialogFragment(): LogoutDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeCancelSubscriptionDialogFragment(): CancelSubscriptionDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationFragment(): NotificationSettingFragment

    @ContributesAndroidInjector
    abstract fun contributeCustomUserNotificationFragment(): CustomUserNotificationFragment

    @ContributesAndroidInjector
    abstract fun contributeAttachmentPreviewFragment(): AttachmentPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeHelpFragment(): HelpFragment

    @ContributesAndroidInjector
    abstract fun contributePermissionDialogFragment(): PermissionDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeMultipleAttachmentPreviewImageFragment(
    ): MultipleAttachmentPreviewImageFragment

    @ContributesAndroidInjector
    abstract fun contributeMultipleAttachmentPreviewVideoFragment(
    ): MultipleAttachmentPreviewVideoFragment

    @ContributesAndroidInjector
    abstract fun contributeTabsPagerFragment(): TabsPagerFragment

}