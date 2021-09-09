package com.naposystems.napoleonchat.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.naposystems.napoleonchat.R
import dagger.android.AndroidInjection

class TabsPagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_tabs_pager)
    }
}