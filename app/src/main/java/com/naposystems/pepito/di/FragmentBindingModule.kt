package com.naposystems.pepito.di

import com.naposystems.pepito.ui.appearanceSettings.AppearanceSettingsFragment
import com.naposystems.pepito.ui.blockedContacts.BlockedContactsFragment
import com.naposystems.pepito.ui.colorScheme.ColorSchemeFragment
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.ui.register.accessPin.AccessPinFragment
import com.naposystems.pepito.ui.register.validateNickname.ValidateNicknameFragment
import com.naposystems.pepito.ui.register.enterCode.EnterCodeFragment
import com.naposystems.pepito.ui.register.sendCode.SendCodeFragment
import com.naposystems.pepito.ui.splash.SplashFragment
import com.naposystems.pepito.ui.status.StatusFragment
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
}