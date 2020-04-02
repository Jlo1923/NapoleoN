package com.naposystems.pepito.ui.baseFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

open class BaseFragment : Fragment() {

    companion object {
        fun newInstance() = BaseFragment()
    }

    @Inject
    open lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: BaseViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.outputControl(Constants.OutputControl.TRUE.state)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(BaseViewModel::class.java)
    }

    open fun validateStateOutputControl() {
        if (viewModel.outputControl.value != Constants.OutputControl.TRUE.state) {
            viewModel.outputControl(Constants.OutputControl.TRUE.state)
        }
    }

}
