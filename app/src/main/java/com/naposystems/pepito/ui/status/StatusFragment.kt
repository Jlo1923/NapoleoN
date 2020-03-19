package com.naposystems.pepito.ui.status

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.StatusFragmentBinding
import com.naposystems.pepito.entity.Status
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
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(StatusViewModel::class.java)

        binding.viewModel = viewModel

        context?.let { context ->

            binding.textInputEditTextStatus.setOnEditorActionListener { view, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (view.text.trim().isNotEmpty()) {
                        viewModel.status.value?.let { listStatus ->
                            if (listStatus.count() < 10) {
                                viewModel.updateStatus(
                                    context,
                                    view.text.toString()
                                )
                                view.clearFocus()
                            } else {
                                Utils.alertDialogInformative(
                                    R.string.text_status_limit,
                                    true,
                                    context,
                                    R.string.text_accept,
                                    clickTopButton = {

                                    }
                                )
                            }
                        }
                    }
                    false
                } else {
                    true
                }
            }

            viewModel.user.value = args.user

            viewModel.status.observe(viewLifecycleOwner, Observer { statusList ->
                if (statusList != null) {
                    selectStatus(statusList)
                    adapter = StatusAdapter(
                        statusList,
                        StatusAdapter.StatusSelectionListener(clickListener = { status ->
                            val textStatus = if (status.resourceId > 0) {
                                context.getString(status.resourceId)
                            } else {
                                status.customStatus
                            }

                            binding.textInputEditTextStatus.setText(textStatus)
                            viewModel.updateStatus(context, textStatus)
                        }, clickDelete = { status, view ->
                            val popup = PopupMenu(context, view)
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

    private fun selectStatus(listStatus : List<Status>) {
        viewModel.user.value?.let { user ->
            context?.let {context ->
                val statusOld = listStatus.find {
                    (it.resourceId != 0) && (context.getString(it.resourceId).trim() == user.status.trim()) ||
                    (it.resourceId == 0) && (it.customStatus.trim() == user.status.trim())
                }

                if (statusOld == null) {
                    val statusByDefect = context.getString(listStatus[0].resourceId)
                    viewModel.updateStatus(context, statusByDefect)
                    binding.textInputEditTextStatus.setText(statusByDefect)
                }
            }
        }
    }
}
