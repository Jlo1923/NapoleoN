package com.naposystems.napoleonchat.utility.extensions

import android.view.View

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show(mustShow: Boolean) {
    if (mustShow) {
        show()
    } else {
        hide()
    }
}