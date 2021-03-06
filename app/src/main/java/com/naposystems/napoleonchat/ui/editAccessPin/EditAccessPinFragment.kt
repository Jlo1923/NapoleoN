package com.naposystems.napoleonchat.ui.editAccessPin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.EditAccessPinFragmentBinding
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.utility.FieldsValidator
import com.naposystems.napoleonchat.utility.Utils

class EditAccessPinFragment : BaseFragment() {

    companion object {
        fun newInstance() = EditAccessPinFragment()
    }

    private lateinit var viewModel: EditAccessPinViewModel

    private lateinit var binding: EditAccessPinFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.edit_access_pin_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.textInputEditTextOldAccessPin.addTextChangedListener(editTextTextWatcher())
        binding.textInputEditTextAccessPin.addTextChangedListener(editTextTextWatcher())
        binding.textInputEditTextConfirmAccessPin.addTextChangedListener(editTextTextWatcher())

        binding.buttonEdit.setOnClickListener {
            binding.textInputLayoutOldAccessPin.error = null
            val newAccessPin = binding.textInputEditTextOldAccessPin.text!!.toString()
            if (viewModel.validateAccessPin(newAccessPin)) {
                val newAccessPin = binding.textInputEditTextAccessPin.text!!.toString()
                if (FieldsValidator.isAccessPinValid(binding.textInputLayoutAccessPin) &&
                    FieldsValidator.isConfirmAccessPinValid(
                        binding.textInputLayoutAccessPin,
                        binding.textInputLayoutConfirmAccessPin
                    )
                ) {
                    viewModel.updateAccessPin(newAccessPin)
                }
            } else {
                binding.textInputLayoutOldAccessPin.error =
                    requireContext().getString(R.string.text_access_pin_not_correspond)
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EditAccessPinViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.accessPinUpdatedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Utils.showToast(
                    requireContext(),
                    getString(R.string.text_access_pin_updated_successfully)
                )
                this.findNavController().popBackStack()
            } else if (it == false) {
                val message = requireContext().getString(R.string.text_error_updating_access_pin)
                Utils.showSimpleSnackbar(binding.coordinator, message, 3)
            }
        })
    }

    private fun editTextTextWatcher() = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.buttonEdit.isEnabled =
                binding.textInputEditTextOldAccessPin.text!!.length >= 4 &&
                        binding.textInputEditTextAccessPin.text!!.length >= 4 &&
                        binding.textInputEditTextConfirmAccessPin.text!!.length >= 4
        }
    }

}
