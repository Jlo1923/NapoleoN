package com.naposystems.pepito.ui.infoAlert

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.InfoAlertFragmentBinding

class InfoAlertFragment : DialogFragment() {

    //    private lateinit var listener: InfoAlertListener
    private lateinit var binding: InfoAlertFragmentBinding

    companion object {
        fun newInstance() = InfoAlertFragment()
    }

//    interface InfoAlertListener {
//        fun onClickInfo()
//    }

    private lateinit var viewModel: InfoAlertViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.info_alert_fragment, container, false
        )

        binding.buttonAccept.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(InfoAlertViewModel::class.java)

        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

//    fun setListener(listener: InfoAlertListener) {
//        this.listener = listener
//    }
}
