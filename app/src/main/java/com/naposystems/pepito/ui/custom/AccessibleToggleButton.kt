package com.naposystems.pepito.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatToggleButton

class AccessibleToggleButton constructor(context: Context, attributeSet: AttributeSet) :
    AppCompatToggleButton(context, attributeSet) {

    private var listener: OnCheckedChangeListener? = null

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        super.setOnCheckedChangeListener(listener)
        this.listener = listener
    }

    fun setChecked(checked: Boolean, notifyListener: Boolean) {
        if (!notifyListener) {
            super.setOnCheckedChangeListener(null)
        }
        super.setChecked(checked)
        if (!notifyListener) {
            super.setOnCheckedChangeListener(listener)
        }
    }

    fun getOnCheckedChangeListener(): OnCheckedChangeListener? {
        return listener
    }
}