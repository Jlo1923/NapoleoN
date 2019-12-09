package com.naposystems.pepito.utility

import android.text.InputFilter
import android.text.Spanned
import java.lang.StringBuilder
import java.util.regex.Pattern

class NicknameInputFilter(pattern: String, listener: OnListener) : InputFilter {

    private val mListener: OnListener = listener
    private val mPattern = Pattern.compile(pattern)
//    private val pattern2 = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$")

    interface OnListener {
        fun onComplete(status: Boolean)
    }
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {

        val matcher = mPattern.matcher(source!!)

        if (!matcher.find()) {
            return ""
        }

        return null
    }

}