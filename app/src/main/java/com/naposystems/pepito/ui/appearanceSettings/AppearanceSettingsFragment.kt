package com.naposystems.pepito.ui.appearanceSettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AppearanceSettingsFragmentBinding
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AppearanceSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = AppearanceSettingsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: AppearanceSettingsViewModel
    private lateinit var binding: AppearanceSettingsFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.appearance_settings_fragment,
            container,
            false
        )

        binding.lifecycleOwner = this

        binding.textViewLanguageSelected.text = getLanguageSelected()

        binding.optionColorScheme.setOnClickListener(colorSchemeClickListener())
        binding.imageButtonColorOptionEndIcon.setOnClickListener(colorSchemeClickListener())

        binding.optionDisplayFormat.setOnClickListener(userDisplayFormatClickListener())
        binding.imageButtonUserDisplayOptionEndIcon.setOnClickListener(
            userDisplayFormatClickListener()
        )

        binding.optionLanguage.setOnClickListener(languageClickListener())
        binding.imageButtonLanguageOptionEndIcon.setOnClickListener(languageClickListener())

        return binding.root
    }

    private fun getLanguageSelected(): String {
        return when (LocaleHelper.getLanguagePreference(context!!)) {
            "de" -> "Deutsch"
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "it" -> "Italiano"
            "pt" -> "Português"
            else -> "English"
        }
    }

    private fun colorSchemeClickListener() = View.OnClickListener {
        findNavController().navigate(
            AppearanceSettingsFragmentDirections
                .actionAppearanceSettingsFragmentToColorSchemeFragment()
        )
    }

    private fun languageClickListener() = View.OnClickListener {
        findNavController().navigate(
            AppearanceSettingsFragmentDirections
                .actionAppearanceSettingsFragmentToLanguageSelectionDialog()
        )
    }

    private fun userDisplayFormatClickListener() = View.OnClickListener {
        val userDisplayFormatDialog = UserDisplayFormatDialogFragment()
        userDisplayFormatDialog.setListener(object :
            UserDisplayFormatDialogFragment.UserDisplayFormatListener {
            override fun onUserDisplayChange() {
                viewModel.getUserDisplayFormat()
            }
        })
        userDisplayFormatDialog.show(childFragmentManager, "UserDisplayFormat")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AppearanceSettingsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getColorScheme()
        viewModel.getUserDisplayFormat()
    }

}
