package com.naposystems.napoleonchat.utility.showCaseManager

import android.app.Activity
import android.view.View
import androidx.fragment.app.FragmentActivity

interface IContractShowCaseManager {
    fun setListener(listener: ShowCaseManager.Listener)
    fun setActivity(activity: FragmentActivity)
    fun dismiss()
    fun setFirstView(view: View)
    fun setSecondView(view: View)
    fun setThirdView(view: View)
    fun setFourthView(view: View)
    fun setFifthView(view: View)
    fun setSixthView(view: View)
    fun setSeventhView(view: View)
    fun setPaused(paused : Boolean)
    fun showFromFirst()
    fun showFromSecond()
    fun showFromThird()
    fun showFromFourth()
    fun showFromFifth()
    fun showFromSixth()
    fun showFromSeventh()
    fun showSixth(callback: () -> Unit)
}