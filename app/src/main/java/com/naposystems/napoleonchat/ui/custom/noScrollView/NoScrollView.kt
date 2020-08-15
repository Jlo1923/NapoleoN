package com.naposystems.napoleonchat.ui.custom.noScrollView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView


class NoScrollView(context: Context, attributeSet: AttributeSet) :
    ScrollView(context, attributeSet) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}