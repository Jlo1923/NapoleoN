package com.naposystems.pepito.ui.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SplashFragmentBinding
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SplashViewModel by viewModels { viewModelFactory }
    private lateinit var binding: SplashFragmentBinding
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

        binding = DataBindingUtil.inflate(inflater, R.layout.splash_fragment, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //region Assignment of Observables
        viewModel.getTimeRequestAccessPin()
        viewModel.getLockTime()
        viewModel.getLockType()
        viewModel.getUnlockTimeApp()
        viewModel.getLockStatus()
        viewModel.getAccountStatus()


        viewModel.timeAccessPin.observe(viewLifecycleOwner, Observer {
            timeAccessPin = it
        })

        viewModel.lockTimeApp.observe(viewLifecycleOwner, Observer {
            lockTime = it
        })

        viewModel.typeLock.observe(viewLifecycleOwner, Observer {
            lockTypeApp = it
        })

        viewModel.unlockTimeApp.observe(viewLifecycleOwner, Observer {
            timeUnlockApp = it
        })

        viewModel.lockStatus.observe(viewLifecycleOwner, Observer {
            lockStatus = it
        })
        //endregion

        viewModel.accountStatus.observe(viewLifecycleOwner, Observer { accountStatus ->
            when (accountStatus) {
                Constants.AccountStatus.NO_ACCOUNT.id -> {
                    binding.viewWhite.visibility = View.GONE
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLandingFragment())
                    viewModel.doneNavigateToLanding()
                }
                Constants.AccountStatus.CODE_VALIDATED.id -> {
                    findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToRegisterFragment()
                    )
                    viewModel.doneNavigateToLanding()
                }
                Constants.AccountStatus.ACCOUNT_CREATED.id -> {
                    when (lockTypeApp) {
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
                            viewModel.doneNavigateToLanding()
                        }
                    }
                }
                Constants.AccountStatus.ACCOUNT_RECOVERED.id -> {
                    viewModel.getUser()
                }
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer { user ->
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToAccessPinFragment(
                    user.nickname,
                    user.displayName,
                    true
                )
            )
            viewModel.doneNavigateToLanding()
        })

        //region Set DefaultPreferences
        viewModel.setDefaultPreferences()
        viewModel.setDefaultLanguage(LocaleHelper.getLanguagePreference(requireContext()))
        setDefaultBiometricsOption()
        //endregion
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


    private fun setDefaultBiometricsOption() {
        val biometricManager = BiometricManager.from(requireContext())

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                viewModel.setDefaultBiometricsOption(Constants.Biometrics.BIOMETRICS_NOT_FOUND.option)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                viewModel.setDefaultBiometricsOption(Constants.Biometrics.WITHOUT_BIOMETRICS.option)
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                viewModel.setDefaultBiometricsOption(Constants.Biometrics.UNLOCK_WITH_FINGERPRINT.option)
            }

        }
    }
    //endregion
}
