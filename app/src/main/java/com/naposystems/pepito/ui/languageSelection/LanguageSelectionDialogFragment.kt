package com.naposystems.pepito.ui.languageSelection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.LanguageSelectionDialogFragmentBinding
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.ui.languageSelection.adapter.LanguageSelectionAdapter
import com.naposystems.pepito.utility.LocaleHelper
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class LanguageSelectionDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LanguageSelectionViewModel by viewModels { viewModelFactory }
    private lateinit var binding: LanguageSelectionDialogFragmentBinding
    private lateinit var adapter: LanguageSelectionAdapter

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
            R.layout.language_selection_dialog_fragment,
            container,
            false
        )

        adapter =
            LanguageSelectionAdapter(
                viewModel.languagesList,
                LanguageSelectionAdapter.LanguageSelectionListener {
                    Timber.d("Buenooo hpta")
                    viewModel.setSelectedLanguage(it)
                    val languageSelected = it
                    changeLocale(languageSelected)
                },
                LocaleHelper.getLanguagePreference(requireContext())
            )

        binding.recyclerViewLanguages.adapter = adapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
    }

    private fun changeLocale(language: Language) {
        Timber.d("changeLocale: ${language.iso}")
        LocaleHelper.setNewLanguage(requireContext(), language)
        requireActivity().recreate()
        dismiss()
    }
}
