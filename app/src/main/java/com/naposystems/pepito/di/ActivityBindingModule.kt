package com.naposystems.pepito.di

import com.naposystems.pepito.ui.conversationCall.ConversationCallActivity
import com.naposystems.pepito.ui.mainActivity.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindConversationCallActivity(): ConversationCallActivity
}