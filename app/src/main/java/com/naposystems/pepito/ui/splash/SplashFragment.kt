package com.naposystems.pepito.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SplashViewModel
    private lateinit var user: User

    //region Variables Access Pin
    private var lockStatus: Int = 0
    private var timeAccessPin: Int = 0
    private var lockTime: Long = 0L
    private var lockTypeApp: Int = 0
    private var timeUnlockApp: Long = 0L
    //endregion

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SplashViewModel::class.java)

        //region Assignment of Observables
        viewModel.getTimeRequestAccessPin()
        viewModel.timeAccessPin.observe(viewLifecycleOwner, Observer {
            timeAccessPin = it
        })

        viewModel.getLockTime()
        viewModel.lockTimeApp.observe(viewLifecycleOwner, Observer {
            lockTime = it
        })

        viewModel.getLockType()
        viewModel.typeLock.observe(viewLifecycleOwner, Observer {
            lockTypeApp = it
        })

        viewModel.getUnlockTimeApp()
        viewModel.unlockTimeApp.observe(viewLifecycleOwner, Observer {
            timeUnlockApp = it
        })

        viewModel.getLockStatus()
        viewModel.lockStatus.observe(viewLifecycleOwner, Observer {
            lockStatus = it
        })
        //endregion

        viewModel.navigateToLanding.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                when (viewModel.getAccountStatus()) {
                    Constants.AccountStatus.CODE_VALIDATED.id -> findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToRegisterFragment()
                    )
                    Constants.AccountStatus.ACCOUNT_CREATED.id -> {
                        when(lockTypeApp) {
                            Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type -> {
                                validateTimeLock()
                            }
                            Constants.LockTypeApp.LOCK_APP_FOR_ATTEMPTS.type -> {
                                validateTimeForUnlockApp()
                            }
                            Constants.LockTypeApp.FOREVER_UNLOCK.type -> {
                                findNavController().navigate(
                                    SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                                )
                            }
                        }
                    }
                    Constants.AccountStatus.ACCOUNT_RECOVERED.id -> {
                        viewModel.getUser()
                        viewModel.user.observe(viewLifecycleOwner, Observer {
                            user = it
                        })
                        findNavController().navigate(
                            SplashFragmentDirections.actionSplashFragmentToAccessPinFragment(
                                user.nickname,
                                user.displayName,
                                true
                            )
                        )
                    }
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


        //region Set DefaultPreferences
        viewModel.setDefaultPreferences()
        viewModel.setDefaultLanguage(LocaleHelper.getLanguagePreference(context!!))
        setDefaultBiometricsOption()
        //endregion

        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    //region Local Methods
    private fun validateTimeForUnlockApp() {
        if (System.currentTimeMillis() >= timeUnlockApp) {
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToEnterPinFragment()
            )
        } else {
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToUnlockAppTimeFragment()
            )
        }
    }

    private fun validateTimeLock() {
        val currentTime = System.currentTimeMillis()

        if (currentTime < lockTime && lockStatus == Constants.LockStatus.UNLOCK.state) {
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToHomeFragment()
            )
        } else {
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToEnterPinFragment()
            )
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun setDefaultBiometricsOption() {
        val biometricManager = BiometricManager.from(context!!)

        when(biometricManager.canAuthenticate()){
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.d("No hay Biometrico en este dispositivo")
                viewModel.setDefaultBiometricsOption(Constants.Biometrics.BIOMETRICS_NOT_FOUND.option)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.d("No hay biometrico asociado")
                viewModel.setDefaultBiometricsOption(Constants.Biometrics.WITHOUT_BIOMETRICS.option)
            }
        }
    }
    //endregion
}
