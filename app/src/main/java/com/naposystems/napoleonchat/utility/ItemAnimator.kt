package com.naposystems.napoleonchat.utility

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class ItemAnimator : DefaultItemAnimator() {

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.let { itemView ->
            itemView.clearAnimation()
            itemView.animate()
                ?.alpha(10F)
                ?.setInterpolator(AccelerateInterpolator(0F))
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        dispatchRemoveFinished(holder)
                    }
                })
                ?.start()
        }
        return false
    }
}