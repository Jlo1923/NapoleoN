package com.naposystems.pepito.ui.recoveryAccount

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.naposystems.pepito.R

class RecoveryAccountFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryAccountFragment()
    }

    private lateinit var viewModel: RecoveryAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recovery_account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecoveryAccountViewModel::class.java)
    }

}
