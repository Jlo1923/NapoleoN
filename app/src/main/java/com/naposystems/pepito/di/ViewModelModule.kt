package com.naposystems.pepito.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naposystems.pepito.ui.appearanceSettings.AppearanceSettingsViewModel
import com.naposystems.pepito.ui.blockedContacts.BlockedContactsViewModel
import com.naposystems.pepito.ui.colorScheme.ColorSchemeViewModel
import com.naposystems.pepito.ui.contactUs.ContactUsViewModel
import com.naposystems.pepito.ui.contacts.ContactsViewModel
import com.naposystems.pepito.ui.conversation.ConversationViewModel
import com.naposystems.pepito.ui.editAccessPin.EditAccessPinViewModel
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionViewModel
import com.naposystems.pepito.ui.mainActivity.MainActivityViewModel
import com.naposystems.pepito.ui.profile.ProfileViewModel
import com.naposystems.pepito.ui.recoveryAccount.RecoveryAccountViewModel
import com.naposystems.pepito.ui.recoveryAccountQuestions.RecoveryAccountQuestionsViewModel
import com.naposystems.pepito.ui.register.accessPin.AccessPinViewModel
import com.naposystems.pepito.ui.register.enterCode.EnterCodeViewModel
import com.naposystems.pepito.ui.register.sendCode.SendCodeViewModel
import com.naposystems.pepito.ui.register.validateNickname.ValidateNicknameViewModel
import com.naposystems.pepito.ui.registerRecoveryAccount.RegisterRecoveryAccountViewModel
import com.naposystems.pepito.ui.registerRecoveryAccountQuestion.RegisterRecoveryAccountQuestionViewModel
import com.naposystems.pepito.ui.securitySettings.SecuritySettingsViewModel
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeViewModel
import com.naposystems.pepito.ui.splash.SplashViewModel
import com.naposystems.pepito.ui.status.StatusViewModel
import com.naposystems.pepito.ui.timeAccessPin.TimeAccessPinDialogViewModel
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.naposystems.pepito.utility.viewModel.ViewModelKey
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
    internal abstract fun binSplashViewModel(viewModel: SplashViewModel): ViewModel

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
    @ViewModelKey(ColorSchemeViewModel::class)
    internal abstract fun bindColorSchemeViewModel(viewModel: ColorSchemeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserDisplayFormatDialogViewModel::class)
    internal abstract fun bindUserDisplayFormatDialogViewModel(viewModel: UserDisplayFormatDialogViewModel): ViewModel

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
    internal abstract fun binRecoveryAccountViewModel(viewModel: RecoveryAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecoveryAccountQuestionsViewModel::class)
    internal abstract fun binRecoveryAccountQuestionsViewModel(viewModel: RecoveryAccountQuestionsViewModel): ViewModel
}