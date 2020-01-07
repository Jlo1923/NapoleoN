package com.naposystems.pepito.ui.generalDialog

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.GeneralDialogFragmentBinding
import java.util.zip.Inflater

class GeneralDialogFragment : Fragment() {

    private lateinit var listener: GeneralDialogFragment
    private lateinit var binding: GeneralDialogFragmentBinding

    companion object {
        fun newInstance() = GeneralDialogFragment()
    }

    interface ActionDialog {
        fun onActionChange()
    }

    private lateinit var viewModel: GeneralDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.general_dialog_fragment, container, false
        )

        binding.buttonCancel.setOnClickListener {

        }

        binding.buttonAccept.setOnClickListener {

        }


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GeneralDialogViewModel::class.java)

    }

    fun setListener(listener: ActionDialog) {

    }

}
