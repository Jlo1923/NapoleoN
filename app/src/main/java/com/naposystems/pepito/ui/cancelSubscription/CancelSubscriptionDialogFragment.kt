package com.naposystems.pepito.ui.cancelSubscription

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
import com.naposystems.pepito.databinding.CancelSubscriptionDialogFragmentBinding
import com.naposystems.pepito.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class CancelSubscriptionDialogFragment : DialogFragment() {

    companion object {
        const val IS_ASKING = 1
        const val IS_SERVICE_CALLED = 2
        const val SERVICE_ANSWER_OK = 3
        const val SERVICE_ANSWER_ERROR = 4
        fun newInstance() = CancelSubscriptionDialogFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: CancelSubscriptionDialogViewModel by viewModels { viewModelFactory }
    private lateinit var binding: CancelSubscriptionDialogFragmentBinding

    private var mLogoutStatus = IS_ASKING
    private var mListener: Listener? = null

    interface Listener {
        fun subscriptionCancelledSuccessfully()
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
            inflater, R.layout.cancel_subscription_dialog_fragment, container, false
        )

        binding.buttonAccept.setSafeOnClickListener {
            when (mLogoutStatus) {
                IS_ASKING -> {
                    viewModel.cancelSubscription()
                }
                SERVICE_ANSWER_OK -> {
                    mListener?.subscriptionCancelledSuccessfully()
                    dismiss()
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

        viewModel.subscriptionStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mLogoutStatus = it
                Timber.d("Subscription: $it")
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
                        binding.textViewMessage.text =
                            "Tu suscripción ha sido cancelada correctamente"
                        binding.buttonAccept.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                        binding.textViewMessage.visibility = View.VISIBLE
                    }
                    SERVICE_ANSWER_ERROR -> {
                        binding.buttonAccept.text = getString(R.string.text_okay)
                        binding.buttonAccept.visibility = View.VISIBLE
                        binding.textViewMessage.text =
                            "Ha ocurrido un error al cancelar tu suscripción, por favor intenta más tarde."
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