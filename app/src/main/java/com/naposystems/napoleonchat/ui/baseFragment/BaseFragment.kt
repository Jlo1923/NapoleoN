package com.naposystems.napoleonchat.ui.baseFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

open class BaseFragment : Fragment() {

    companion object {
        fun newInstance() = BaseFragment()
    }

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: BaseViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.outputControl(Constants.OutputControl.TRUE.state)
    }

    open fun validateStateOutputControl() {
        if (viewModel.outputControl.value != Constants.OutputControl.TRUE.state) {
            viewModel.outputControl(Constants.OutputControl.TRUE.state)
        }
    }

}
