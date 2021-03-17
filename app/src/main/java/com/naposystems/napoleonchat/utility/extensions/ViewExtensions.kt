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

fun showViews(vararg views: View) {
    for (view in views) {
        view.show()
    }
}

fun hideViews(vararg views: View) {
    for (view in views) {
        view.hide()
    }
}