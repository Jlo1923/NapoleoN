package com.naposystems.pepito.ui.status

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.StatusFragmentBinding
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.status.adapter.StatusAdapter
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class StatusFragment : Fragment() {

    companion object {
        fun newInstance() = StatusFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var viewModel: StatusViewModel
    private lateinit var binding: StatusFragmentBinding
    private lateinit var adapter: StatusAdapter
    private lateinit var user: User
    private val args: StatusFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.status_fragment, container, false
        )

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(StatusViewModel::class.java)

        binding.viewModel = viewModel

        binding.textInputEditTextStatus.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.updateStatus(UpdateUserInfoReqDTO(status = view.text.toString()))
                view.clearFocus()
                false
            } else {
                true
            }
        }

        viewModel.user.value = args.user

        viewModel.status.observe(viewLifecycleOwner, Observer { statusList ->
            adapter = StatusAdapter(statusList, StatusAdapter.StatusSelectionListener {
                val status = context!!.getString(it.resourceId)
                binding.textInputEditTextStatus!!.setText(status)

                val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                    status = status
                )

                viewModel.updateStatus(updateUserInfoReqDTO)

            })

            binding.recyclerViewStatus.adapter = adapter
        })

        viewModel.errorGettingStatus.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val message = getString(R.string.text_error_getting_local_status)

                Utils.showSimpleSnackbar(binding.coordinator, message, 3)
            }
        })

        viewModel.errorUpdatingStatus.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })
    }

}
