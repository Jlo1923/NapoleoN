package com.naposystems.napoleonchat.ui.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.HelpFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils

class HelpFragment : Fragment() {

    companion object {
        fun newInstance() = HelpFragment()
    }

    private lateinit var binding: HelpFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.help_fragment, container, false
        )
        binding.lifecycleOwner = this

        binding.optionContactUs.setOnClickListener(contactUsClickListener())
        binding.imageButtonContactOptionEndIcon.setOnClickListener(contactUsClickListener())

        binding.optionFrequentQuestions.setOnClickListener(frequentQuestionsClickListener())
        binding.imageButtonFrequentOptionEndIcon.setOnClickListener(frequentQuestionsClickListener())

        binding.optionTermsAndConditions.setOnClickListener(termsAndConditionsClickListener())
        binding.imageButtonTermsOptionEndIcon.setOnClickListener(termsAndConditionsClickListener())

        binding.optionAbout.setOnClickListener(aboutClickListener())
        binding.imageButtonAboutOptionEndIcon.setOnClickListener(aboutClickListener())

        binding.optionRepeatShowCase.setOnClickListener(repeatShowCaseClickListener())
        binding.imageButtonRepeatShowCaseOptionEndIcon.setOnClickListener(
            repeatShowCaseClickListener()
        )

        return binding.root
    }

    private fun frequentQuestionsClickListener() = View.OnClickListener {
        val uri: Uri = Uri.parse(Constants.URL_FREQUENT_QUESTIONS)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun termsAndConditionsClickListener() = View.OnClickListener {
        val uri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun aboutClickListener() = View.OnClickListener {
        findNavController().navigate(
            HelpFragmentDirections.actionHelpFragmentToAboutFragment()
        )
    }

    private fun contactUsClickListener() = View.OnClickListener {
        findNavController().navigate(
            HelpFragmentDirections.actionHelpFragmentToContactUsFragment()
        )
    }

    private fun repeatShowCaseClickListener() = View.OnClickListener {
        Utils.generalDialog(
            requireContext().getString(R.string.text_repeat_tutorial),
            requireContext().getString(R.string.text_message_dialog_repeat_tutorial),
            true,
            childFragmentManager,
            getString(R.string.text_yes),
            getString(R.string.text_no)
        ) {
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())

            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_FIRST_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SECOND_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_THIRD_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_FOURTH_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_FIFTH_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SIXTH_STEP_HAS_BEEN_SHOW,
                false
            )
            sharedPreferencesManager.putBoolean(
                Constants.SharedPreferences.PREF_SHOW_CASE_SEVENTH_STEP_HAS_BEEN_SHOW,
                false
            )

            findNavController().popBackStack()
        }
    }

}
