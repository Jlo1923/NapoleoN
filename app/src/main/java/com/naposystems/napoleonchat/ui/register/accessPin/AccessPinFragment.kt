package com.naposystems.napoleonchat.ui.register.accessPin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AccessPinFragmentBinding
import com.naposystems.napoleonchat.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.subscription.BillingClientLifecycle
import com.naposystems.napoleonchat.utility.FieldsValidator
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.DefaultPreferencesViewModel
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
    private val viewModelDefaultPreferences: DefaultPreferencesViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(billingClientLifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory)
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

        if (recoveredAccount) {
            binding.textViewTitle.text = getString(R.string.text_reset_access_pin_title)
            binding.buttonRegister.text = getString(R.string.text_recovery_account)
        }

        binding.buttonRegister.setOnClickListener {
            validateAccessPin()
        }

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar {}
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
                viewModelDefaultPreferences.setDefaultPreferences()
                billingClientLifecycle.queryPurchasesHistory()
                findNavController().navigate(
                    AccessPinFragmentDirections.actionAccessPinFragmentToHomeFragment()
                )
                viewModel.onOpenedHomeFragment()
            }
        })

        billingClientLifecycle.purchasesHistory.observe(
            viewLifecycleOwner,
            Observer { purchasesHistory ->
                purchasesHistory?.let {
                    val subscription = purchasesHistory.isNotEmpty()
                    viewModel.setFreeTrialPref(subscription)
                }
            })

        binding.textInputEditTextAccessPin.addTextChangedListener(textWatcherAccessPin())
        binding.textInputEditTextConfirmAccessPin.addTextChangedListener(textWatcherConfirmAccessPin())

        return binding.root
    }

    private fun validateAccessPin() {
        disableAllWidgets()

        val itsAccessPinOk = FieldsValidator.isAccessPinValid(binding.textInputLayoutAccessPin)
        val itsConfirmAccessPinOk = FieldsValidator.isConfirmAccessPinValid(
            binding.textInputLayoutAccessPin,
            binding.textInputLayoutConfirmAccessPin
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

        val defaultStatus = requireContext().getString(R.string.text_status_available)

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

    private fun textWatcherAccessPin(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            FieldsValidator.isAccessPinValid(binding.textInputLayoutAccessPin)
        }
    }

    private fun textWatcherConfirmAccessPin(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            FieldsValidator.isConfirmAccessPinValid(
                binding.textInputLayoutAccessPin,
                binding.textInputLayoutConfirmAccessPin
            )
        }
    }

}
