package com.naposystems.napoleonchat.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.StatusFragmentBinding
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.status.adapter.StatusAdapter
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.showToast
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import javax.inject.Inject


class StatusFragment : BaseFragment() {

    companion object {
        fun newInstance() = StatusFragment()
    }

    @Inject
    lateinit var handlerDialog: HandlerDialog

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager


    private val viewModel: StatusViewModel by viewModels { viewModelFactory }

    private lateinit var binding: StatusFragmentBinding

    private lateinit var adapter: StatusAdapter

    private val args: StatusFragmentArgs by navArgs()

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

        binding.viewModel = viewModel

        textInputEditTextStatusSetOnEditorActionListener()

        viewModel.user.value = args.user

        observeStatus()

        viewModel.errorGettingStatus.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                showToast(requireContext(), getString(R.string.text_error_getting_local_status))
            }
        })

        viewModel.errorUpdatingStatus.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showToast(requireContext(), it.toString())
            }
        })

        viewModel.statusUpdatedSuccessfully.observe(viewLifecycleOwner, Observer { isUpdated ->
            if (isUpdated == true) {
                Utils.hideKeyboard(binding.coordinator)
                showToast(requireContext(), getString(R.string.text_status_updated_successfully))
            }
        })
    }

    private fun observeStatus() {
        viewModel.statusEntity.observe(viewLifecycleOwner, Observer { statusList ->
            if (statusList != null) {
                context?.let {
                    if (statusList.count() <= 0) {
                        statusList.add(StatusEntity(1, getString(R.string.text_status_available)))
                        statusList.add(StatusEntity(2, getString(R.string.text_status_busy)))
                        statusList.add(StatusEntity(3, getString(R.string.text_status_in_meeting)))
                        statusList.add(
                            StatusEntity(
                                4,
                                getString(R.string.text_status_only_messages)
                            )
                        )
                        statusList.add(StatusEntity(5, getString(R.string.text_status_sleeping)))
                        statusList.add(
                            StatusEntity(
                                6,
                                getString(R.string.text_status_only_emergency)
                            )
                        )
                        if (args.user.status != getString(R.string.text_status_available) &&
                            args.user.status != getString(R.string.text_status_busy) &&
                            args.user.status != getString(R.string.text_status_in_meeting) &&
                            args.user.status != getString(R.string.text_status_only_messages) &&
                            args.user.status != getString(R.string.text_status_sleeping) &&
                            args.user.status != getString(R.string.text_status_only_emergency)
                        ) {
                            statusList.add(StatusEntity(7, customStatus = args.user.status))
                        }
                        viewModel.insertStatus(statusList)
                    }

                    statusList.forEach { status ->
                        when (status.id) {
                            1 -> status.status = getString(R.string.text_status_available)
                            2 -> status.status = getString(R.string.text_status_busy)
                            3 -> status.status = getString(R.string.text_status_in_meeting)
                            4 -> status.status = getString(R.string.text_status_only_messages)
                            5 -> status.status = getString(R.string.text_status_sleeping)
                            6 -> status.status = getString(R.string.text_status_only_emergency)
                            else -> Unit
                        }
                    }

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
                viewModel.statusEntity.value?.let { listStatus ->
                    if (listStatus.count() < 10) {
                        viewModel.updateStatus(textStatus)
                        binding.textInputEditTextStatus.clearFocus()
                    } else {
                        handlerDialog.alertDialogInformative(
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

    private fun selectStatus(listStatusEntities: List<StatusEntity>) {
        viewModel.user.value?.let { user ->
            val statusOld = listStatusEntities.find {
                (it.status.isNotEmpty()) && (it.status.trim() == user.status.trim()) ||
                        (it.status.isEmpty()) && (it.customStatus.trim() == user.status.trim())
            }

            if (statusOld == null) {
                val statusByDefect = listStatusEntities[0].status
                viewModel.updateStatus(statusByDefect)
                binding.textInputEditTextStatus.setText(statusByDefect)
            }
        }
    }

}
