package com.naposystems.pepito.ui.status

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.StatusFragmentBinding
import com.naposystems.pepito.entity.Status
import com.naposystems.pepito.ui.status.adapter.StatusAdapter
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.adapters.showToast
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
    private val args: StatusFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.status_fragment, container, false
        )

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(StatusViewModel::class.java)

        binding.viewModel = viewModel

        textInputEditTextStatusSetOnEditorActionListener()

        viewModel.user.value = args.user

        observeStatus()

        viewModel.errorGettingStatus.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                showToast(getString(R.string.text_error_getting_local_status))
            }
        })

        viewModel.errorUpdatingStatus.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showToast(it.toString())
            }
        })

        viewModel.statusUpdatedSuccessfully.observe(viewLifecycleOwner, Observer { isUpdated ->
            if (isUpdated == true) {
                Utils.hideKeyboard(binding.coordinator)
                showToast(getString(R.string.text_status_updated_successfully))
            }
        })
    }

    private fun observeStatus() {
        viewModel.status.observe(viewLifecycleOwner, Observer { statusList ->
            if (statusList != null) {
                selectStatus(statusList)
                adapter = StatusAdapter(
                    statusList,
                    StatusAdapter.StatusSelectionListener(clickListener = { status ->
                        val textStatus = if (status.status.isNotEmpty()) {
                            status.status
                        } else {
                            status.customStatus
                        }

                        binding.textInputEditTextStatus.setText(textStatus)
                        viewModel.updateStatus(textStatus)
                    }, clickDelete = { status, view ->
                        val popup = PopupMenu(requireContext(), view)
                        popup.menuInflater.inflate(R.menu.menu_popup_status, popup.menu)

                        popup.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.delete_status -> {
                                    viewModel.deleteStatus(status)
                                }
                            }
                            true
                        }
                        popup.show()
                    })
                )
                binding.recyclerViewStatus.adapter = adapter
            }
        })
    }

    private fun textInputEditTextStatusSetOnEditorActionListener() {
        binding.textInputEditTextStatus.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createStatus()
                false
            } else {
                true
            }
        }
    }

    private fun createStatus() {
        binding.textInputEditTextStatus.text?.toString()?.let { textStatus ->
            if (textStatus.trim().isNotEmpty()) {
                viewModel.status.value?.let { listStatus ->
                    if (listStatus.count() < 10) {
                        viewModel.updateStatus(textStatus)
                        binding.textInputEditTextStatus.clearFocus()
                    } else {
                        Utils.alertDialogInformative(
                            getString(R.string.text_alert_failure),
                            getString(R.string.text_status_limit),
                            true,
                            requireContext(),
                            R.string.text_accept,
                            clickTopButton = {

                            }
                        )
                    }
                }
            }
        }
    }

    private fun selectStatus(listStatus: List<Status>) {
        viewModel.user.value?.let { user ->
            val statusOld = listStatus.find {
                (it.status.isNotEmpty()) && (it.status.trim() == user.status.trim()) ||
                        (it.status.isEmpty()) && (it.customStatus.trim() == user.status.trim())
            }

            if (statusOld == null) {
                val statusByDefect = listStatus[0].status
                viewModel.updateStatus(statusByDefect)
                binding.textInputEditTextStatus.setText(statusByDefect)
            }
        }
    }

    private fun showToast(string : String) {
        val vwToast: Toast = Toast.makeText(
            requireContext(),
            string,
            Toast.LENGTH_SHORT
        )
        val tv = vwToast.view.findViewById<View>(android.R.id.message) as TextView
        tv.gravity = Gravity.CENTER
        tv.textSize = 14F
        vwToast.show()
    }
}
