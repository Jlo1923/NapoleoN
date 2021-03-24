package com.naposystems.napoleonchat.ui.baseFragment

import android.content.Intent
import androidx.fragment.app.viewModels
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class BaseFragment : DaggerFragment() {

    companion object {
        fun newInstance() = BaseFragment()
    }

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: BaseViewModel by viewModels { viewModelFactory }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.outputControl(Constants.OutputControl.TRUE.state)
    }

    open fun validateStateOutputControl() {
        if (viewModel.outputControl.value != Constants.OutputControl.TRUE.state) {
            viewModel.outputControl(Constants.OutputControl.TRUE.state)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.connectSocket()
    }
}
