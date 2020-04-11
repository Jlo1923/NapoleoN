package com.naposystems.pepito.ui.colorScheme

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ColorSchemeFragmentBinding
import com.naposystems.pepito.ui.colorScheme.adapter.ColorSchemeAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ColorSchemeFragment : Fragment() {

    companion object {
        fun newInstance() = ColorSchemeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ColorSchemeViewModel
    private lateinit var binding: ColorSchemeFragmentBinding
    private lateinit var adapter: ColorSchemeAdapter

    var theme : Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.color_scheme_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            theme = when(checkedId){
                R.id.radioButton_light_napoleon -> Constants.ThemesApplication.LIGHT_NAPOLEON.theme
                R.id.radioButton_dark_napoleon -> Constants.ThemesApplication.DARK_NAPOLEON.theme
                R.id.radioButton_black_gold_alloy -> Constants.ThemesApplication.BLACK_GOLD_ALLOY.theme
                R.id.radioButton_cold_ocean -> Constants.ThemesApplication.COLD_OCEAN.theme
                R.id.radioButton_camouflage -> Constants.ThemesApplication.CAMOUFLAGE.theme
                R.id.radioButton_purple_bluebonnets -> Constants.ThemesApplication.PURPLE_BLUEBONNETS.theme
                R.id.radioButton_pink_dream -> Constants.ThemesApplication.PINK_DREAM.theme
                else -> Constants.ThemesApplication.CLEAR_SKY.theme
            }
            viewModel.setTheme(theme)
        }

        binding.imageButtonSaveConfiguration.setOnClickListener {
            viewModel.saveTheme(theme)
            if(theme == Constants.ThemesApplication.DARK_NAPOLEON.theme){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            activity?.recreate()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ColorSchemeViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getActualTheme()

        viewModel.theme.observe(viewLifecycleOwner, Observer { theme ->
            when(theme) {
                Constants.ThemesApplication.LIGHT_NAPOLEON.theme -> binding.radioButtonLightNapoleon.isChecked = true
                Constants.ThemesApplication.DARK_NAPOLEON.theme -> binding.radioButtonDarkNapoleon.isChecked = true
                Constants.ThemesApplication.BLACK_GOLD_ALLOY.theme -> binding.radioButtonBlackGoldAlloy.isChecked = true
                Constants.ThemesApplication.COLD_OCEAN.theme -> binding.radioButtonColdOcean.isChecked = true
                Constants.ThemesApplication.CAMOUFLAGE.theme -> binding.radioButtonCamouflage.isChecked = true
                Constants.ThemesApplication.PURPLE_BLUEBONNETS.theme -> binding.radioButtonPurpleBluebonnets.isChecked = true
                Constants.ThemesApplication.PINK_DREAM.theme -> binding.radioButtonPinkDream.isChecked = true
                Constants.ThemesApplication.CLEAR_SKY.theme -> binding.radioButtonClearSky.isChecked = true
            }
        })
    }
}
