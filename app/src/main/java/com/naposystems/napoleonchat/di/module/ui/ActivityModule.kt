package com.naposystems.napoleonchat.di.module.ui

import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.multi.MultipleAttachmentActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeConversationCallActivity(): ConversationCallActivity

    @ContributesAndroidInjector
    abstract fun contributeMultipleAttachmentActivity(): MultipleAttachmentActivity
}