package com.naposystems.pepito.ui.languageSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.LanguageSelectionDialogFragmentBinding
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.ui.languageSelection.adapter.LanguageSelectionAdapter
import com.naposystems.pepito.utility.LocaleHelper

class LanguageSelectionDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: LanguageSelectionViewModel
    private lateinit var binding: LanguageSelectionDialogFragmentBinding
    private lateinit var adapter: LanguageSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModelFactory = LanguageSelectionViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(LanguageSelectionViewModel::class.java)
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
                    viewModel.setSelectedLanguage(it)
                    val languageSelected = it
                    changeLocale(languageSelected)
                },
                LocaleHelper.getLanguagePreference(context!!)
            )

        binding.recyclerViewLanguages.adapter = adapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
    }

    private fun changeLocale(language: Language) {
        LocaleHelper.setNewLanguage(context!!, language)
        activity?.recreate()
        dismiss()
    }
}
