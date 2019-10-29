package com.naposystems.pepito.ui.languageSelection

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.LanguageSelectionFragmentBinding
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.ui.languageSelection.adapter.LanguageSelectionAdapter
import com.naposystems.pepito.utility.LocaleHelper

class LanguageSelectionFragment : DialogFragment() {

    companion object {
        fun newInstance() = LanguageSelectionFragment()
    }

    private lateinit var viewModel: LanguageSelectionViewModel
    private lateinit var binding: LanguageSelectionFragmentBinding
    private lateinit var adapter: LanguageSelectionAdapter

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewModelFactory = LanguageSelectionViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(LanguageSelectionViewModel::class.java)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.language_selection_fragment,
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
                })

        binding.recyclerViewLanguages.adapter = adapter
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        viewModel.selectedLanguage.observe(this, Observer {
            adapter.updateSelectedLanguage(it)
        })

        return binding.root
    }

    private fun changeLocale(language: Language) {
        LocaleHelper.setNewLanguage(context!!, language)
        activity?.recreate()

        dismiss()
    }

}
