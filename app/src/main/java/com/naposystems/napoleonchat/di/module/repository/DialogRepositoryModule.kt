package com.naposystems.napoleonchat.di.module.repository

import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogRepository
import com.naposystems.napoleonchat.dialog.accountAttack.AccountAttackDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogRepository
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogRepository
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogRepository
import com.naposystems.napoleonchat.dialog.changeParams.ChangeParamsDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogRepository
import com.naposystems.napoleonchat.dialog.logout.LogoutDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogRepository
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogRepository
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogRepositoryImp
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogRepository
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogRepositoryImp
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
}