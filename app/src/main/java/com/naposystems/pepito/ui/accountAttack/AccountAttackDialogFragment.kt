package com.naposystems.pepito.ui.accountAttack

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AccountAttackDialogFragmentBinding
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AccountAttackDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = AccountAttackDialogFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AccountAttackDialogViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: AccountAttackDialogFragmentBinding

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
            R.layout.account_attack_dialog_fragment,
            container,
            false
        )

        isCancelable = false

        binding.buttonIAm.setOnClickListener {
            dismiss()
            viewModel.resetExistingAttack()
        }

        binding.buttonIAmNot.setOnClickListener {
            viewModel.blockAttack()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let {
            it.attributes.windowAnimations = R.style.DialogAnimation
            it.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        viewModel.closeModal.observe(viewLifecycleOwner, Observer {
            if (it == true){
                dismiss()
            }
        })

    }
}
