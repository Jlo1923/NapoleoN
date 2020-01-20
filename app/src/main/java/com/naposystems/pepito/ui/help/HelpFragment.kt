package com.naposystems.pepito.ui.help

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.HelpFragmentBinding
import com.naposystems.pepito.utility.Constants

class HelpFragment : Fragment() {

    companion object {
        fun newInstance() = HelpFragment()
    }

    private lateinit var binding: HelpFragmentBinding
    private lateinit var viewModel: HelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.help_fragment, container, false
        )
        binding.lifecycleOwner = this

        binding.optionFrequentQuestions.setOnClickListener(frequentQuestionsClickListener())
        binding.imageButtonFrequentOptionEndIcon.setOnClickListener(frequentQuestionsClickListener())

        binding.optionTermsAndConditions.setOnClickListener(termsAndConditionsClickListener())
        binding.imageButtonTermsOptionEndIcon.setOnClickListener(termsAndConditionsClickListener())

        binding.optionAbout.setOnClickListener(aboutClickListener())
        binding.imageButtonAboutOptionEndIcon.setOnClickListener(aboutClickListener())

        binding.optionContactUs.setOnClickListener(contactUsClickListener())
        binding.imageButtonContactOptionEndIcon.setOnClickListener(contactUsClickListener())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HelpViewModel::class.java)
    }

    private fun frequentQuestionsClickListener() = View.OnClickListener {
        val uri: Uri = Uri.parse(Constants.URL_FREQUENT_QUESTIONS)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(context!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun termsAndConditionsClickListener() = View.OnClickListener {
        val uri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(context!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun aboutClickListener() = View.OnClickListener {
        this.findNavController().navigate(
            HelpFragmentDirections.actionHelpFragmentToAboutFragment()
        )
    }

    private fun contactUsClickListener() = View.OnClickListener {
        findNavController().navigate(
            HelpFragmentDirections.actionHelpFragmentToContactUsFragment()
        )
    }

}
