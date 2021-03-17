package com.naposystems.napoleonchat.utility.anims

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show

fun View.animShowSlideUp(functionOnEndAnimation: () -> Unit = {}) {

    val animation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_up_in)

    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {
            show()
        }

        override fun onAnimationEnd(animation: Animation?) {
            functionOnEndAnimation.invoke()
        }
    })

    startAnimation(animation)
}

fun View.animShowSlideDown(functionOnEndAnimation: () -> Unit = {}) {

    val animation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_down_in)

    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            show()
            functionOnEndAnimation.invoke()
        }
    })

    startAnimation(animation)
}

fun View.animHideSlideDown(functionOnEndAnimation: () -> Unit = {}) {

    val animation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_down_out)

    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            hide()
            functionOnEndAnimation.invoke()
        }
    })

    startAnimation(animation)
}

fun View.animHideSlideUp(functionOnEndAnimation: () -> Unit = {}) {

    val animation = AnimationUtils.loadAnimation(context, R.anim.anim_slide_up_out)

    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationEnd(animation: Animation?) {
            hide()
            functionOnEndAnimation.invoke()
        }
    })

    startAnimation(animation)
}
