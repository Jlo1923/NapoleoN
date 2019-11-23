package com.naposystems.pepito.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        viewModel.navigateToLanding.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                when (getAccountStatus()) {
                    Constants.CODE_VALIDATED -> findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToRegisterFragment()
                    )
                    Constants.ACCOUNT_CREATED -> findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                    )
                    else -> findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLandingFragment())
                }

                viewModel.doneNavigateToLanding()
            }
        })

        Handler().postDelayed({
            context?.let {
                viewModel.onLoadingTimeEnd()
            }
        }, TimeUnit.SECONDS.toMillis(3))

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
            LocaleHelper.getLanguagePreference(context!!)
        )

        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    private fun getAccountStatus(): Int {
        val defaultDefaultCode = 0

        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            defaultDefaultCode
        )
    }

}
