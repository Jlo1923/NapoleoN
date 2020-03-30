package com.naposystems.pepito.ui.userDisplayFormat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.UserDisplayFormatDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class UserDisplayFormatDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = UserDisplayFormatDialogFragment()
    }

    interface UserDisplayFormatListener {
        fun onUserDisplayChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: UserDisplayFormatDialogViewModel by viewModels { viewModelFactory }
    private lateinit var binding: UserDisplayFormatDialogFragmentBinding
    private lateinit var listener: UserDisplayFormatListener
    private var format: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.user_display_format_dialog_fragment, container, false
        )

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->

            format = when (checkedId) {
                R.id.radioButton_name_and_nickname -> Constants.UserDisplayFormat.NAME_AND_NICKNAME.format
                R.id.radioButton_only_name -> Constants.UserDisplayFormat.ONLY_NAME.format
                R.id.radioButton_only_nickname -> Constants.UserDisplayFormat.ONLY_NICKNAME.format
                else -> Constants.UserDisplayFormat.NAME_AND_NICKNAME.format
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            viewModel.setUserDisplayFormat(format)
            listener.onUserDisplayChange()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        viewModel.getUserDisplayFormat()

        viewModel.userDisplayFormat.observe(viewLifecycleOwner, Observer {
            this.format = it
            when (it) {
                Constants.UserDisplayFormat.NAME_AND_NICKNAME.format ->
                    binding.radioButtonNameAndNickname.isChecked = true
                Constants.UserDisplayFormat.ONLY_NAME.format ->
                    binding.radioButtonOnlyName.isChecked = true
                Constants.UserDisplayFormat.ONLY_NICKNAME.format ->
                    binding.radioButtonOnlyNickname.isChecked = true
            }
        })
    }

    fun setListener(listener: UserDisplayFormatListener) {
        this.listener = listener
    }

}
