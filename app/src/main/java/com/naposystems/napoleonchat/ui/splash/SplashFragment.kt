package com.naposystems.napoleonchat.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SplashFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.LocaleHelper
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesSharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.DaggerFragment
import timber.log.Timber
import javax.inject.Inject

class SplashFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SplashViewModel by viewModels { viewModelFactory }

    private val defaultPreferencesSharedViewModel: DefaultPreferencesSharedViewModel by viewModels { viewModelFactory }

    private var _binding: SplashFragmentBinding? = null

    private val binding get() = _binding!!

    //region Variables Access Pin
    private var lockStatus: Int = 0
    private var timeAccessPin: Int = 0
    private var lockTime: Long = 0L
    private var lockTypeApp: Int = 0
    private var timeUnlockApp: Long = 0L
    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Bindeo del fragmento
        _binding = SplashFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

                    viewModel.clearData()

                    Timber.d("AccountStatus ToLanding")

                    binding.viewWhite.visibility = View.GONE
                    findNavController().navigate(
                        R.id.action_splashFragment_to_landingFragment
                    )
                    viewModel.doneNavigateToLanding()
                }

                Constants.AccountStatus.CODE_VALIDATED.id -> {

                    Timber.d("AccountStatus ToRegister")

                    findNavController().navigate(
                        R.id.action_splashFragment_to_validateNicknameFragment
                    )
                    viewModel.doneNavigateToLanding()
                }

                Constants.AccountStatus.ACCOUNT_RECOVERED.id -> {

                    Timber.d("lockTypeApp ACCOUNT_RECOVERED")

                    viewModel.getUser()
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
                                R.id.action_splashFragment_to_homeFragment
                            )
                            viewModel.doneNavigateToLanding()
                        }
                    }
                }
            }
        })

        viewModel.userEntity.observe(viewLifecycleOwner, Observer { user ->
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
        defaultPreferencesSharedViewModel.setDefaultPreferences()
        viewModel.setDefaultLanguage(LocaleHelper.getLanguagePreference(requireContext()))
        setDefaultBiometricsOption()
        //endregion
    }

    //region Local Methods
    private fun validateTimeForUnlockApp() {
        if (System.currentTimeMillis() >= timeUnlockApp) {
            findNavController().navigate(
                R.id.action_splashFragment_to_enterPinFragment
            )
        } else {
            findNavController().navigate(
                R.id.action_splashFragment_to_unlockAppTimeFragment
            )
        }
    }

    private fun validateTimeLock() {

        val currentTime = System.currentTimeMillis()

        if (currentTime < lockTime && lockStatus == Constants.LockStatus.UNLOCK.state) {
            findNavController().navigate(
                R.id.action_splashFragment_to_homeFragment
            )
        } else {
            findNavController().navigate(
                R.id.action_splashFragment_to_enterPinFragment
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
