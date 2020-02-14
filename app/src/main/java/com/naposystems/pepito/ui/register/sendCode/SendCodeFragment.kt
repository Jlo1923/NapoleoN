package com.naposystems.pepito.ui.register.sendCode

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SendCodeFragmentBinding
import com.naposystems.pepito.dto.sendCode.SendCodeReqDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SendCodeFragment : Fragment() {

    companion object {
        fun newInstance() = SendCodeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: SendCodeViewModel
    private lateinit var binding: SendCodeFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SendCodeViewModel::class.java)

        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.send_code_fragment, container, false
        )

        binding.viewModel = viewModel

        binding.buttonSendCode.setOnClickListener {
            binding.viewSwitcher.showNext()

            val firebaseId = sharedPreferencesManager
                .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")

            val sendCodeReqDTO = SendCodeReqDTO(
                firebaseId
            )
            viewModel.onSendCodePressed(sendCodeReqDTO)
        }

        viewModel.openEnterCode.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(SendCodeFragmentDirections.actionSendCodeFragmentToEnterCodeFragment())
                viewModel.onEnterCodeOpened()
            }
        })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                binding.viewSwitcher.showPrevious()
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

}
