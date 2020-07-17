package com.naposystems.pepito.utility.showCaseManager

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.view.View
import com.khryzyz.spotlight.SpotlightView
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import timber.log.Timber
import java.util.*

class ShowCaseManager : IContractShowCaseManager {

    private lateinit var activity: Activity
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private var firstView: View? = null
    private var firstId: String = UUID.randomUUID().toString()
    private var secondView: View? = null
    private var secondId: String = UUID.randomUUID().toString()
    private var thirdView: View? = null
    private var thirdId: String = UUID.randomUUID().toString()
    private var fourthView: View? = null
    private var fourthId: String = UUID.randomUUID().toString()
    private var fifthView: View? = null
    private var fifthId: String = UUID.randomUUID().toString()
    private var sixthView: View? = null
    private var sixthId: String = UUID.randomUUID().toString()
    private var seventhView: View? = null
    private var seventhId: String = UUID.randomUUID().toString()

    private var mListener: Listener? = null

    interface Listener {
        fun openSecuritySettings()
    }

    private fun showShowCase(
        activity: Activity,
        view: View?,
        uniqueId: String,
        title: String,
        subtitle: String,
        headingTextSize: Int = 28,
        callback: () -> Unit
    ) {
        if (view != null) {
            SpotlightView.Builder(activity)
                .introAnimationDuration(200)
                .enableRevealAnimation(true)
                .performClick(false)
                .fadeinTextDuration(200)
                .headingTvColor(Color.parseColor("#d8a608"))
                .headingTvSize(headingTextSize)
                .headingTvText(title)
                .subHeadingTvColor(Color.parseColor("#f2f2f2"))
                .subHeadingTvSize(16)
                .subHeadingTvText(subtitle)
                .maskColor(Color.parseColor("#dc000000"))
                .target(view)
                .lineAnimDuration(200)
                .lineAndArcColor(Color.parseColor("#d8a608"))
                .dismissOnTouch(false)
                .dismissOnBackPress(false)
                .enableDismissAfterShown(false)
                .buttonText("Ok")
                .showButton(true)
                .buttonColorBackground(Color.parseColor("#dc000000"))
                .buttonSize(16)
                .usageId(uniqueId)
                .setListener {
                    Timber.d("SpotlightView: $it")
                    callback()
                }
                .show()
        } else {
            Timber.e("La vista está nula")
        }
    }

    private fun showFirst(callback: () -> Unit) {
        showShowCase(
            activity,
            firstView,
            firstId,
            activity.getString(R.string.show_case_first_title),
            activity.getString(R.string.show_case_first_subtitle)
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_FIRST_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

    private fun showSecond(callback: () -> Unit) {
        showShowCase(
            activity,
            secondView,
            secondId,
            activity.getString(R.string.show_case_second_title),
            activity.getString(R.string.show_case_second_subtitle)
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SECOND_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

    private fun showThird(callback: () -> Unit) {
        showShowCase(
            activity,
            thirdView,
            thirdId,
            activity.getString(R.string.show_case_third_title),
            activity.getString(R.string.show_case_third_subtitle)
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_THIRD_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

    private fun showFourth(callback: () -> Unit) {
        showShowCase(
            activity,
            fourthView,
            fourthId,
            activity.getString(R.string.show_case_fourth_title),
            activity.getString(R.string.show_case_fourth_subtitle)
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_FOURTH_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

    private fun showFifth(callback: () -> Unit) {
        fourthView?.performClick()
        Handler().postDelayed({
            showShowCase(
                activity,
                fifthView,
                fifthId,
                activity.getString(R.string.show_case_fifth_title),
                activity.getString(R.string.show_case_fifth_subtitle)
            ) {
                sharedPreferencesManager.putBoolean(
                    Constants.SharedPreferences.PREF_SHOW_CASE_FIFTH_STEP_HAS_BEEN_SHOW,
                    true
                )
                callback()
            }
        }, 500)
    }

    private fun showSeventh(callback: () -> Unit) {
        showShowCase(
            activity,
            seventhView,
            seventhId,
            activity.getString(R.string.show_case_seventh_title),
            activity.getString(R.string.show_case_seventh_subtitle),
            24
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SEVENTH_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun setActivity(activity: Activity) {
        this.activity = activity
        sharedPreferencesManager = SharedPreferencesManager(activity)
    }

    override fun setFirstView(view: View) {
        this.firstView = view
    }

    override fun setSecondView(view: View) {
        this.secondView = view
    }

    override fun setThirdView(view: View) {
        this.thirdView = view
    }

    override fun setFourthView(view: View) {
        this.fourthView = view
    }

    override fun setFifthView(view: View) {
        this.fifthView = view
    }

    override fun setSixthView(view: View) {
        this.sixthView = view
    }

    override fun setSeventhView(view: View) {
        this.seventhView = view
    }

    override fun showFromFirst() {
        val firstHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_FIRST_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!firstHasBeenShow) {
            showFirst {
                showSecond { showThird { showFourth { showFifth { mListener?.openSecuritySettings() } } } }
            }
        } else {
            showFromSecond()
        }
    }

    override fun showFromSecond() {
        val secondHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_SECOND_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!secondHasBeenShow) {
            showSecond { showThird { showFourth { showFifth { mListener?.openSecuritySettings() } } } }
        } else {
            showFromThird()
        }
    }

    override fun showFromThird() {
        val thirdHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_THIRD_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!thirdHasBeenShow) {
            showThird { showFourth { showFifth { mListener?.openSecuritySettings() } } }
        } else {
            showFromFourth()
        }
    }

    override fun showFromFourth() {
        val fourthHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_FOURTH_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!fourthHasBeenShow) {
            showFourth { showFifth { mListener?.openSecuritySettings() } }
        } else {
            showFromFifth()
        }
    }

    override fun showFromFifth() {
        val fifthHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_FIFTH_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!fifthHasBeenShow) {
            showFifth { mListener?.openSecuritySettings() }
        } else {
            showFromSixth()
        }
    }

    override fun showFromSixth() {
        val sixthHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_SIXTH_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!sixthHasBeenShow) {
            mListener?.openSecuritySettings()
        }
    }

    override fun showFromSeventh() {
        val seventhHasBeenShow = sharedPreferencesManager.getBoolean(
            Constants.SharedPreferences.PREF_SHOW_CASE_SEVENTH_STEP_HAS_BEEN_SHOW,
            false
        )

        if (!seventhHasBeenShow) {
            showSeventh { Unit }
        }
    }

    override fun showSixth(callback: () -> Unit) {
        showShowCase(
            activity,
            sixthView,
            sixthId,
            activity.getString(R.string.show_case_sixth_title),
            activity.getString(R.string.show_case_sixth_subtitle)
        ) {
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SIXTH_STEP_HAS_BEEN_SHOW,
                true
            )
            callback()
        }
    }

}