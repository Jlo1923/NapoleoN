package com.naposystems.pepito.ui.editAccessPin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EditAccessPinFragmentBinding
import com.naposystems.pepito.utility.FieldsValidator
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class EditAccessPinFragment : Fragment() {

    companion object {
        fun newInstance() = EditAccessPinFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: EditAccessPinViewModel
    private lateinit var binding: EditAccessPinFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

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
                Toast.makeText(
                    requireContext(),
                    R.string.text_access_pin_updated_successfully,
                    Toast.LENGTH_LONG
                ).show()
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
