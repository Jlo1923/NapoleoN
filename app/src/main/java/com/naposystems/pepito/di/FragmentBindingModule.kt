package com.naposystems.pepito.di

import com.naposystems.pepito.ui.appearanceSettings.AppearanceSettingsFragment
import com.naposystems.pepito.ui.blockedContacts.BlockedContactsFragment
import com.naposystems.pepito.ui.colorScheme.ColorSchemeFragment
import com.naposystems.pepito.ui.contacts.ContactsFragment
import com.naposystems.pepito.ui.contactUs.ContactUsFragment
import com.naposystems.pepito.ui.conversation.ConversationFragment
import com.naposystems.pepito.ui.editAccessPin.EditAccessPinFragment
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.ui.recoveryAccount.RecoveryAccountFragment
import com.naposystems.pepito.ui.register.accessPin.AccessPinFragment
import com.naposystems.pepito.ui.register.enterCode.EnterCodeFragment
import com.naposystems.pepito.ui.register.sendCode.SendCodeFragment
import com.naposystems.pepito.ui.register.validateNickname.ValidateNicknameFragment
import com.naposystems.pepito.ui.registerRecoveryAccount.RegisterRecoveryAccountFragment
import com.naposystems.pepito.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionFragment
import com.naposystems.pepito.ui.securitySettings.SecuritySettingsFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.splash.SplashFragment
import com.naposystems.pepito.ui.status.StatusFragment
import com.naposystems.pepito.ui.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogFragment
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
    abstract fun bindColorSchemeFragment(): ColorSchemeFragment

    @ContributesAndroidInjector
    abstract fun bindUserDisplayFormatDialogFragment(): UserDisplayFormatDialogFragment

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
}