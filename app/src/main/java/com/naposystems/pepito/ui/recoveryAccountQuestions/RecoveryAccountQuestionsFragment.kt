package com.naposystems.pepito.ui.recoveryAccountQuestions

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import timber.log.Timber

class RecoveryAccountQuestionsFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryAccountQuestionsFragment()
    }

    private lateinit var viewModel: RecoveryAccountQuestionsViewModel
    private val args: RecoveryAccountQuestionsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Timber.d(args.questions.toString())

        return inflater.inflate(R.layout.recovery_account_questions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(RecoveryAccountQuestionsViewModel::class.java)

    }

}
