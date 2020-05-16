package com.naposystems.pepito.ui.register.validateNickname

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ValidateNicknameFragmentBinding
import com.naposystems.pepito.dto.validateNickname.ValidateNicknameReqDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FieldsValidator
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class ValidateNicknameFragment : Fragment() {

    companion object {
        fun newInstance() = ValidateNicknameFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ValidateNicknameViewModel by viewModels { viewModelFactory }
    private lateinit var binding: ValidateNicknameFragmentBinding

    private var itsNicknameAvailable = true

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.validate_nickname_fragment,
                container,
                false
            )

        binding.viewModel = viewModel

        binding.buttonNext.setOnClickListener {

            val itsNicknameOk: Boolean =
                FieldsValidator.isNicknameValid(binding.textInputLayoutNickname)
            val itsDisplayName: Boolean =
                FieldsValidator.isDisplayNameValid(binding.textInputLayoutDisplayName)

            if (itsNicknameOk && itsDisplayName) {
                this.findNavController()
                    .navigate(
                        ValidateNicknameFragmentDirections
                            .actionRegisterFragmentToAccessPinFragment(
                                viewModel.nickName.value!!.toLowerCase(Locale.getDefault()),
                                viewModel.displayName.value!!,
                                false
                            )
                    )
            }
        }

        viewModel.openTermsAndConditions.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                openTermsAndConditions()
                viewModel.onTermsAndConditionsLaunched()
            }
        })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                Snackbar.make(binding.coordinator, it, Snackbar.LENGTH_SHORT).show()
            }
        })

        viewModel.itsNicknameValid.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it == false) {
                    itsNicknameAvailable = true
                    binding.buttonNext.isEnabled = true

                    setDrawableTextInput(binding.textInputEditTextNickname)
                    binding.textInputLayoutNickname.error = null
                } else {
                    itsNicknameAvailable = false
                    binding.buttonNext.isEnabled = false
                    resetDrawableTextInput(binding.textInputEditTextNickname)
                    binding.textInputLayoutNickname.error =
                        getString(R.string.text_nickname_unavailable)
                }
            }
        })

        binding.textInputEditTextNickname.addTextChangedListener(textWatcherNickname())

        return binding.root
    }

    private fun openTermsAndConditions() {
        val termsAndConditionsUri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, termsAndConditionsUri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun setDrawableTextInput(textInputEditText: TextInputEditText) {
        val drawable = requireContext().resources.getDrawable(
            R.drawable.ic_language_selected,
            requireContext().theme
        )

        textInputEditText.apply {
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                drawable,
                null
            )
        }
    }

    private fun resetDrawableTextInput(textInputEditText: TextInputEditText) {
        textInputEditText.apply {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    private fun textWatcherNickname(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            //Nothing
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //Nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val nicknameRegex: Pattern =
                Pattern.compile(Constants.RegularExpressions.NICKNAME)

            if (nicknameRegex.matcher(s!!).find()) {
                val string = s.toString().toLowerCase(Locale.getDefault())
                val lastChar = string.last()
                val lastCharIsDigit = lastChar.isDigit()
                val lastCharIsNumberOne = if (lastCharIsDigit) {
                    Character.getNumericValue(lastChar) == 1
                } else {
                    false
                }
                val restOfString = string.substring(IntRange(0, string.length - 2))

                val restContainsNumber = restOfString.contains(Regex("\\d"))

                if (lastCharIsNumberOne && !restContainsNumber) {
                    viewModel.setNoValidNickname()
                } else {
                    val validateNicknameReqDTO = ValidateNicknameReqDTO(string)
                    viewModel.validateNickname(validateNicknameReqDTO)
                }
            } else {
                resetDrawableTextInput(binding.textInputEditTextNickname)
            }
        }
    }
}
