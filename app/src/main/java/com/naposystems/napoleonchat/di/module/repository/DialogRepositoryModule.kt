package com.naposystems.napoleonchat.di.module.repository

import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogRepository
import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogRepository
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogRepository
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogRepository
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.languageSelection.LanguageSelectionDialogRepository
import com.naposystems.napoleonchat.dialog.languageSelection.LanguageSelectionDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogRepository
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.muteConversation.MuteConversationDialogRepository
import com.naposystems.napoleonchat.dialog.muteConversation.MuteConversationDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogRepository
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogRepository
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogRepository
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogRepository
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogRepositoryImp
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogRepository
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogRepositoryImp
import dagger.Binds
import dagger.Module

@Module
abstract class DialogRepositoryModule {

    @Binds
    abstract fun bindAccountAttackDialogRepository(repository: AccountAttackDialogRepositoryImp): AccountAttackDialogRepository

    @Binds
    abstract fun bindChangeParamsDialogRepository(repository: ChangeParamsDialogRepositoryImp): ChangeParamsDialogRepository

    @Binds
    abstract fun bindUserDisplayFormatDialogRepository(repository: UserDisplayFormatDialogRepositoryImp): UserDisplayFormatDialogRepository

    @Binds
    abstract fun bindTimeFormatDialogRepository(repository: TimeFormatDialogRepositoryImp): TimeFormatDialogRepository

    @Binds
    abstract fun bindActivateBiometricsDialogRepository(repository: ActivateBiometricsDialogRepositoryImp): ActivateBiometricsDialogRepository

    @Binds
    abstract fun bindCancelSubscriptionDialogRepository(repository: CancelSubscriptionDialogRepositoryImp): CancelSubscriptionDialogRepository

    @Binds
    abstract fun bindLogoutDialogRepository(repository: LogoutDialogRepositoryImp): LogoutDialogRepository

    @Binds
    abstract fun bindTimeAccessPinDialogRepository(repository: TimeAccessPinDialogRepositoryImp): TimeAccessPinDialogRepository

    @Binds
    abstract fun bindLanguageSelectionRepository(repository: LanguageSelectionDialogRepositoryImp): LanguageSelectionDialogRepository

    @Binds
    abstract fun bindConversationMuteRepository(repository: MuteConversationDialogRepositoryImp): MuteConversationDialogRepository

    @Binds
    abstract fun bindSelfDestructTimeRepository(repository: SelfDestructTimeDialogRepositoryImp): SelfDestructTimeDialogRepository

    @Binds
    abstract fun bindSelfDestructTimeMessageNotSentRepository(repository: SelfDestructTimeMessageNotSentDialogRepositoryImp): SelfDestructTimeMessageNotSentDialogRepository

}