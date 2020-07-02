package com.naposystems.pepito.ui.languageSelection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.LanguageSelectionDialogFragmentBinding
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.ui.deletionDialog.DeletionMessagesDialogFragment
import com.naposystems.pepito.ui.languageSelection.adapter.LanguageSelectionAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

const val LOCATION = "location"
class LanguageSelectionDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(location : Int) = LanguageSelectionDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(LOCATION, location)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
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
                LanguageSelectionAdapter.LanguageSelectionListener { languageSelected ->
                    arguments?.getInt(LOCATION)?.let { location ->
                        viewModel.setSelectedLanguage(languageSelected, location)
                    }
                },
                LocaleHelper.getLanguagePreference(requireContext())
            )

        binding.recyclerViewLanguages.adapter = adapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation

        viewModel.selectedLanguage.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                changeLocale(it)
            }
        })

        viewModel.errorUpdatingLanguage.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.showToast(getString(R.string.text_error_updating_language))
            }
        })
    }

    private fun changeLocale(language: Language) {
        LocaleHelper.setNewLanguage(requireContext(), language)
        requireActivity().recreate()
        dismiss()
    }
}
