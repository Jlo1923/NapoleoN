package com.naposystems.pepito.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionViewModel
import com.naposystems.pepito.ui.mainActivity.MainActivityViewModel
import com.naposystems.pepito.ui.register.accessPin.AccessPinViewModel
import com.naposystems.pepito.ui.register.validateNickname.ValidateNicknameViewModel
import com.naposystems.pepito.ui.register.enterCode.EnterCodeViewModel
import com.naposystems.pepito.ui.register.sendCode.SendCodeViewModel
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
    @ViewModelKey(AccessPinViewModel::class)
    internal abstract fun bindAccessPinViewModel(viewModel: AccessPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    internal abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel
}