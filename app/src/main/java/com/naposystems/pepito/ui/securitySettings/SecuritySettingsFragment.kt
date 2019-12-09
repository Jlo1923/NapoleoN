package com.naposystems.pepito.ui.securitySettings

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SecuritySettingsFragmentBinding
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SecuritySettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SecuritySettingsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SecuritySettingsViewModel
    private lateinit var binding: SecuritySettingsFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.security_settings_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.optionMessageSelfDestruct.setOnClickListener(optionMessageClickListener())
        binding.imageButtonMessageOptionEndIcon.setOnClickListener(optionMessageClickListener())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SecuritySettingsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getSelfDestructTime()
    }

    private fun optionMessageClickListener() = View.OnClickListener {
        val dialog = SelfDestructTimeDialogFragment()
        dialog.setListener(object : SelfDestructTimeDialogFragment.SelfDestructTimeListener {
            override fun onSelfDestructTimeChange() {
                viewModel.getSelfDestructTime()
            }
        })
        dialog.show(childFragmentManager, "SelfDestructTime")
    }

}
