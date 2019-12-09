package com.naposystems.pepito.ui.colorScheme

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ColorSchemeFragmentBinding
import com.naposystems.pepito.databinding.ColorSchemeItemBinding
import com.naposystems.pepito.entity.Theme
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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ColorSchemeViewModel::class.java)

        viewModel.theme.observe(viewLifecycleOwner, Observer {
            setAdapter(it)
        })
    }

    private fun setAdapter(selectedTheme: Int) {
        val themeList = listOf(
            Theme(1, context!!.getString(R.string.text_light_theme), false),
            Theme(2, context!!.getString(R.string.text_dark_theme), false)
        )

        for (theme in themeList) {
            if (theme.id == selectedTheme) {
                theme.isSelected = true
            }
        }

        adapter = ColorSchemeAdapter(themeList, ColorSchemeAdapter.ColorSchemeListener {
            val theme = when (it.id) {
                Constants.ColorScheme.LIGHT_THEME.scheme -> AppCompatDelegate.MODE_NIGHT_NO
                Constants.ColorScheme.DARK_THEME.scheme -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }


            viewModel.setTheme(it.id)
            viewModel.getActualTheme()
            AppCompatDelegate.setDefaultNightMode(theme)
        })

        binding.recyclerViewThemes.adapter = adapter
    }

}
