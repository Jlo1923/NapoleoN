package com.naposystems.napoleonchat.ui.logout

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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.LogoutDialogFragmentBinding
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class LogoutDialogFragment : DialogFragment() {

    companion object {
        const val IS_ASKING = 1
        const val IS_SERVICE_CALLED = 2
        const val SERVICE_ANSWER_OK = 3
        const val SERVICE_ANSWER_ERROR = 4
        fun newInstance() = LogoutDialogFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: LogoutDialogViewModel by viewModels { viewModelFactory }
    private lateinit var binding: LogoutDialogFragmentBinding

    private var mLogoutStatus = IS_ASKING
    private var mListener: Listener? = null

    interface Listener {
        fun logOutSuccessfully()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        isCancelable = false

        binding = DataBindingUtil.inflate(
            inflater, R.layout.logout_dialog_fragment, container, false
        )

        binding.buttonAccept.setSafeOnClickListener {
            when (mLogoutStatus) {
                IS_ASKING -> {
                    viewModel.logOut()
                }
                SERVICE_ANSWER_OK -> {
                    mListener?.logOutSuccessfully()
                }
                else -> dismiss()
            }
        }

        binding.buttonCancel.setSafeOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.window?.let {
            it.attributes.windowAnimations = R.style.DialogAnimation
            it.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        viewModel.logoutStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mLogoutStatus = it
                when (it) {
                    IS_SERVICE_CALLED -> {
                        binding.buttonCancel.visibility = View.GONE
                        binding.buttonAccept.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewMessage.visibility = View.GONE
                    }
                    SERVICE_ANSWER_OK -> {
                        binding.buttonCancel.visibility = View.GONE
                        binding.buttonAccept.text = getString(R.string.text_okay)
                        binding.textViewTitle.text = getString(R.string.text_closed_session)
                        binding.textViewMessage.text = getString(R.string.text_closed_session_desc)
                        binding.buttonAccept.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.textViewMessage.visibility = View.VISIBLE
                    }
                    SERVICE_ANSWER_ERROR -> {
                        binding.buttonAccept.text = getString(R.string.text_okay)
                        binding.buttonAccept.visibility = View.VISIBLE
                        binding.textViewMessage.text = getString(R.string.text_error_logout)
                        binding.progressBar.visibility = View.GONE
                        binding.textViewMessage.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

}