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
                    Constants.AccountStatus.CODE_VALIDATED.id -> findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToRegisterFragment()
                    )
                    Constants.AccountStatus.ACCOUNT_CREATED.id -> findNavController().navigate(
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
        }, TimeUnit.SECONDS.toMillis(1))

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
            LocaleHelper.getLanguagePreference(context!!)
        )

        setDefaultTheme()
        setDefaultUserDisplayFormat()

        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    private fun getAccountStatus(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS
        )
    }

    private fun setDefaultTheme(){
        val default = sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)

        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_COLOR_SCHEME,
                Constants.ColorScheme.LIGHT_THEME.scheme
            )
        }
    }

    private fun setDefaultUserDisplayFormat(){
        val default = sharedPreferencesManager
            .getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)

        if (default == 0){
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT,
                Constants.UserDisplayFormat.NAME_AND_NICKNAME.format
            )
        }
    }

}
