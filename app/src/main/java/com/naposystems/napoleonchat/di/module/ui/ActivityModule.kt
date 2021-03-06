package com.naposystems.napoleonchat.di.module.ui

import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.home.TabsPagerActivity
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.multi.MultipleAttachmentActivity
import com.naposystems.napoleonchat.ui.multipreview.MultipleAttachmentPreviewActivity
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

    @ContributesAndroidInjector
    abstract fun contributeMultipleAttachmentPreviewActivity(): MultipleAttachmentPreviewActivity

    @ContributesAndroidInjector
    abstract fun contributeMultipleTabsPagerActivity(): TabsPagerActivity
}