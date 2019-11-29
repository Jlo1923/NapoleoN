package com.naposystems.pepito.ui.register.accessPin

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AccessPinFragmentBinding
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.*
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AccessPinFragment : Fragment() {

    companion object {
        fun newInstance() = AccessPinFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: AccessPinViewModel
    private lateinit var binding: AccessPinFragmentBinding
    private lateinit var nickname: String
    private lateinit var displayName: String
    private lateinit var snackbarUtils: SnackbarUtils
    private val args: AccessPinFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AccessPinViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.access_pin_fragment,
            container,
            false
        )

        binding.viewModel = viewModel

        nickname = args.nickname
        displayName = args.displayName

        binding.buttonRegister.setOnClickListener {
            validateAccessPin()
        }

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
                enableAllWidgets()
                binding.viewSwitcher.showPrevious()
            }
        })

        viewModel.userCreatedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {

                val firebaseId = sharedPreferencesManager.getString(
                    Constants.SharedPreferences.PREF_FIREBASE_ID,
                    ""
                )

                val user = User(
                    firebaseId,
                    nickname,
                    displayName,
                    viewModel.accessPin.value!!,
                    "",
                    "",
                    ""
                )
                viewModel.createUser(user)
            }
        })

        viewModel.userCreatedLocallySuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.openHomeFragment()
            }
        })

        viewModel.userCreationError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                Utils.showSimpleSnackbar(binding.coordinator, it, 2)
            }
        })

        viewModel.openHomeFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
                    Constants.ACCOUNT_CREATED
                )
                findNavController()
                    .navigate(AccessPinFragmentDirections.actionAccessPinFragmentToHomeFragment())
                viewModel.onOpenedHomeFragment()
            }
        })

        return binding.root
    }

    private fun validateAccessPin() {
        disableAllWidgets()

        val itsAccessPinOk = FieldsValidator.isAccessPinValid(binding.textInputLayoutAccessPin)
        val itsConfirmAccessPinOk = FieldsValidator.isConfirmAccessPinValid(
            binding.textInputLayoutConfirmAccessPin,
            binding.textInputEditTextAccessPin.text.toString()
        )

        if (itsAccessPinOk && itsConfirmAccessPinOk) {
            binding.viewSwitcher.showNext()

            val firebaseId = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID,
                ""
            )

            val languageIso = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
                ""
            )

            val createAccountReqDTO = CreateAccountReqDTO(
                firebaseId,
                displayName,
                nickname,
                languageIso,
                viewModel.accessPin.value!!,
                viewModel.confirmAccessPin.value!!
            )

            viewModel.createAccount(createAccountReqDTO)
        } else {
            enableAllWidgets()
        }
    }

    private fun disableAllWidgets() {
        binding.textInputLayoutAccessPin.isEnabled = false
        binding.textInputLayoutConfirmAccessPin.isEnabled = false
        binding.buttonRegister.isEnabled = false
    }

    private fun enableAllWidgets() {
        binding.textInputLayoutAccessPin.isEnabled = true
        binding.textInputLayoutConfirmAccessPin.isEnabled = true
        binding.buttonRegister.isEnabled = true
    }

}
