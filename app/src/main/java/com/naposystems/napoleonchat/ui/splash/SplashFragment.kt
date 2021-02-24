package com.naposystems.napoleonchat.ui.splash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SplashFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.LocaleHelper
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class SplashFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SplashViewModel by viewModels { viewModelFactory }
    private val viewModelDefaultPreferences: DefaultPreferencesViewModel by viewModels { viewModelFactory }
    private lateinit var binding: SplashFragmentBinding

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

            Timber.d("AccountStatus {$accountStatus}")

            when (accountStatus) {
                Constants.AccountStatus.NO_ACCOUNT.id -> {


                    Timber.d("AccountStatus ToLanding")

                    binding.viewWhite.visibility = View.GONE
                    findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLandingFragment())
                    viewModel.doneNavigateToLanding()
                }
                Constants.AccountStatus.CODE_VALIDATED.id -> {

                    Timber.d("AccountStatus ToRegister")

                    findNavController().navigate(
                        SplashFragmentDirections.actionSplashFragmentToRegisterFragment()
                    )
                    viewModel.doneNavigateToLanding()
                }
                Constants.AccountStatus.ACCOUNT_CREATED.id -> {

                    Timber.d("AccountStatus Created")

                    when (lockTypeApp) {
                        Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type -> {

                            Timber.d("lockTypeApp LOCK_FOR_TIME_REQUEST_PIN")

                            validateTimeLock()
                        }
                        Constants.LockTypeApp.LOCK_APP_FOR_ATTEMPTS.type -> {

                            Timber.d("lockTypeApp LOCK_APP_FOR_ATTEMPTS")

                            validateTimeForUnlockApp()
                        }
                        Constants.LockTypeApp.FOREVER_UNLOCK.type -> {

                            Timber.d("lockTypeApp FOREVER_UNLOCK")

                            findNavController().navigate(
                                SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                            )
                            viewModel.doneNavigateToLanding()
                        }
                    }
                }
                Constants.AccountStatus.ACCOUNT_RECOVERED.id -> {

                    Timber.d("lockTypeApp ACCOUNT_RECOVERED")

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
        viewModelDefaultPreferences.setDefaultPreferences()
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
