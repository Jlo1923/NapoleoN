package com.naposystems.pepito.ui.register.accessPin

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AccessPinFragmentBinding
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.ui.mainActivity.MainActivity
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
    private var recoveredAccount: Boolean = false
    private lateinit var snackbarUtils: SnackbarUtils
    private val args: AccessPinFragmentArgs by navArgs()

    private lateinit var firebaseId: String

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

        firebaseId = viewModel.getFirebaseId()

        //Asignación de argumentos pasados por navigate
        nickname = args.nickname
        displayName = args.displayName
        recoveredAccount = args.isRecoveredAccount

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
            if (it != null) {
                viewModel.createUser(it)
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
                viewModel.createdUserPref()
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

            if (recoveredAccount) {
                updateAccessPin()
            } else {
                createAccount()
            }

        } else {
            enableAllWidgets()
        }
    }

    private fun createAccount() {
        val languageIso = viewModel.getLanguage()

        val defaultStatus = context!!.getString(R.string.text_status_available)

        val createAccountReqDTO = CreateAccountReqDTO(
            firebaseId,
            displayName,
            nickname,
            languageIso,
            viewModel.accessPin.value!!,
            viewModel.confirmAccessPin.value!!,
            defaultStatus
        )

        viewModel.createAccount(createAccountReqDTO)
    }

    private fun updateAccessPin() {
        viewModel.updateAccessPin(
            binding.textInputEditTextAccessPin.text.toString(),
            firebaseId
        )
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
