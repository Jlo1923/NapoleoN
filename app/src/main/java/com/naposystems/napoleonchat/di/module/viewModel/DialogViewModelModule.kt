package com.naposystems.napoleonchat.di.module.viewModel

import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogViewModel
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogViewModel
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogViewModel
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogViewModel
import com.naposystems.napoleonchat.dialog.languageSelection.LanguageSelectionDialogViewModel
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogViewModel
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogViewModel
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogViewModel
import com.naposystems.napoleonchat.dialog.muteConversation.MuteConversationDialogViewModel
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DialogViewModelModule {

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
    @ViewModelKey(AccountAttackDialogViewModel::class)
    internal abstract fun bindAccountAttackDialogViewModel(viewModel: AccountAttackDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangeParamsDialogViewModel::class)
    internal abstract fun bindChangeFakesDialogViewModel(viewModel: ChangeParamsDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivateBiometricsDialogViewModel::class)
    internal abstract fun bindActivateBiometricsViewModel(viewModel: ActivateBiometricsDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeAccessPinDialogViewModel::class)
    internal abstract fun bindTimeAccessPinDialogViewModel(viewModel: TimeAccessPinDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TimeFormatDialogViewModel::class)
    internal abstract fun bindTimeFormatDialogViewModel(viewModel: TimeFormatDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserDisplayFormatDialogViewModel::class)
    internal abstract fun bindUserDisplayFormatViewModel(viewModel: UserDisplayFormatDialogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguageSelectionDialogViewModel::class)
    internal abstract fun bindLanguageSelectionViewModel(viewModel: LanguageSelectionDialogViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MuteConversationDialogViewModel::class)
    internal abstract fun bindMuteConversationViewModel(viewModel: MuteConversationDialogViewModel): ViewModel

}