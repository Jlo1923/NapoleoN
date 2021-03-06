package com.naposystems.napoleonchat.ui.contactUs

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ContactUsFragmentBinding
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.model.CategoryPqrs
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ContactUsFragment : BaseFragment() {

    companion object {
        fun newInstance() = ContactUsFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ContactUsViewModel by viewModels { viewModelFactory }
    private lateinit var binding: ContactUsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.contact_us_fragment, container, false
        )

        val listCategories = listOf(
            CategoryPqrs(1, requireContext().getString(R.string.text_report_problem)),
            CategoryPqrs(2, requireContext().getString(R.string.text_commentary)),
            CategoryPqrs(3, requireContext().getString(R.string.text_suggestion))
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.contact_us_item,
            R.id.textView_category_item,
            listCategories
        )

        binding.spinnerCategory.adapter = adapter

        binding.textInputEditTextMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        binding.textInputLayoutMessage.error = null
                        if (it.length >= 20) {
                            binding.buttonSendPqrs.isEnabled = true
                        } else {
                            binding.textInputLayoutMessage.error =
                                getString(R.string.text_min_twenty_char)
                            binding.buttonSendPqrs.isEnabled = false
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.buttonSendPqrs.setOnClickListener {
            val selectedItem = binding.spinnerCategory.selectedItem

            if (selectedItem is CategoryPqrs) {

                val contactUsReqDTO = ContactUsReqDTO(
                    selectedItem.id,
                    binding.textInputEditTextMessage.text.toString(),
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    BuildConfig.VERSION_NAME
                )

                viewModel.sendPqrs(contactUsReqDTO)
                disableForm()
            }
        }

        return binding.root
    }

    private fun disableForm() {
        binding.viewSwitcher.showNext()
        Utils.hideKeyboard(binding.textInputEditTextMessage)
        binding.textInputLayoutMessage.isEnabled = false
    }

    private fun enableForm() {
        binding.viewSwitcher.showPrevious()
        binding.textInputLayoutMessage.isEnabled = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.pqrsCreatingErrors.observe(viewLifecycleOwner, Observer {
            SnackbarUtils(binding.coordinator, it).showSnackbar{}
            enableForm()
        })

        viewModel.pqrsCreatedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.spinnerCategory.setSelection(0)
                binding.textInputLayoutMessage.isEnabled = true
                binding.textInputEditTextMessage.setText("")

                val message = requireContext().getString(R.string.text_pqrs_create)

                Utils.showSimpleSnackbar(binding.coordinator, message, 3)

                binding.viewSwitcher.showPrevious()
            }
        })
    }

}
