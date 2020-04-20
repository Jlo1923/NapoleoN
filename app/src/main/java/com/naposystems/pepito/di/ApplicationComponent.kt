package com.naposystems.pepito.di

import android.app.Application
import com.naposystems.pepito.app.NapoleonApplication
import com.naposystems.pepito.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class, ApplicationModule::class, ViewModelModule::class,
        FragmentBindingModule::class, ActivityBindingModule::class, SplashModule::class,
        SendCodeModule::class, EnterCodeModule::class, LanguageSelectionModule::class,
        ValidateNicknameModule::class, CreateAccountModule::class, ProfileModule::class,
        StatusModule::class, BlockedContactsModule::class, AppearanceSettingsModule::class,
        ColorSchemeModule::class, UserDisplayFormatModule::class, SelfDestructTime::class,
        SecuritySettingsModule::class, EditAccessPinModule::class, TimeAccessPinModule::class,
        ContactsModule::class, ContactUsModule::class, ConversationModule::class,
        RegisterRecoveryAccountModule::class, RecoveryAccountModule::class,
        RecoveryAccountQuestionsModule::class, RegisterRecoveryAccountQuestionModule::class,
        AddContactModule::class, HomeModule::class, SocketModule::class, UserProfileShareModule::class,
        ActivateBiometricsModule::class, EnterPinModule::class, UnlockAppTimeModule::class,
        ContactProfileModule::class, MuteConversationModule::class, ChangeParamsModule::class,
        ShareContactModule::class, ContactProfileShareModule::class,
        ContactRepositoryShareModule::class, BaseModule::class,
        SelfDestructTimeMessageNotSentModule::class, AttachmentGalleryFolderModule::class,
        AttachmentGalleryModule::class, ValidatePasswordPreviousRecoveryAccountModule::class,
        RecoveryOlderAccountQuestionsModule::class, AccountAttackDialogModule::class,
        SubscriptionModule::class, NapoleonKeyboardGifModule::class,
        AttachmentLocationModule::class, ConversationCallModule::class,
        RoomModule::class]
)
interface ApplicationComponent : AndroidInjector<NapoleonApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun create(application: Application): Builder

        fun build(): ApplicationComponent
    }

}