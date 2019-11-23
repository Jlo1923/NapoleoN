package com.naposystems.pepito.ui.appearanceSettings

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.naposystems.pepito.R

class AppearanceSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = AppearanceSettingsFragment()
    }

    private lateinit var viewModel: AppearanceSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.appearance_settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AppearanceSettingsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
