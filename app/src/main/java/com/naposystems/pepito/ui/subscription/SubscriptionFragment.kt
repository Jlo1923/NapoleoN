package com.naposystems.pepito.ui.subscription


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SubscriptionFragmentBinding
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import com.naposystems.pepito.model.typeSubscription.TypeSubscription
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubscriptionFragment : Fragment() {

    companion object {
        fun newInstance() = SubscriptionFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SubscriptionViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var binding: SubscriptionFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private var subscriptionUser: SubscriptionUser? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.subscription_fragment,
            container,
            false
        )

        binding.imageButtonPaypal.setOnClickListener {
            sendPayment()
            binding.viewSwitcher.showNext()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getTypeSubscription()

        observeTypeSubscription()

        viewModel.getTypeSubscriptionError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
                binding.viewSwitcher.showPrevious()
            }
        })

        viewModel.subscriptionUrl.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                findNavController().navigate(
                    SubscriptionFragmentDirections
                        .actionSubscriptionFragmentToSubscriptionPaymentFragment(it.url)
                )
                viewModel.resetViewModel()
            }
        })

        viewModel.sendPaymentError.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
            binding.viewSwitcher.showPrevious()
        })
    }

    private fun observeTypeSubscription() {
        viewModel.typeSubscription.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val netDate: Date
                subscriptionUser = viewModel.getSubscription()

                if (System.currentTimeMillis() > viewModel.getFreeTrial()) {
                    if (subscriptionUser?.dateExpires != 0L) {
                        if (System.currentTimeMillis() > subscriptionUser!!.dateExpires) {
                            binding.textViewSubscriptionActual.text =
                                getString(R.string.text_expired_subscription)
                        } else {
                            val currentSubscription = it.find { typeSubscription ->
                                typeSubscription.id == subscriptionUser?.subscriptionId
                            }?.description
                            binding.textViewSubscriptionActual.text = currentSubscription
                        }
                    } else {
                        binding.textViewSubscriptionActual.text =
                            getString(R.string.text_free_trial_expired)
                    }
                    netDate = Date(subscriptionUser!!.dateExpires)
                } else {
                    netDate = Date(viewModel.getFreeTrial())
                    binding.textViewSubscriptionActual.text = getString(R.string.text_trial_period)
                }

                binding.textViewSubscriptionExpiration.text = sdf.format(netDate)

                val selectSubscription = getString(R.string.text_select_subscription)
                val newListSubscription = it.toMutableList()

                newListSubscription.add(0, TypeSubscription(0, selectSubscription, 0, 0, 0))

                val adapter = ArrayAdapter(
                    context!!,
                    R.layout.subscription_item,
                    R.id.textView_subscription_item,
                    newListSubscription
                )

                binding.spinnerPayment.adapter = adapter
            }
        })
    }

    private fun sendPayment() {
        val selectedPaymentId = binding.spinnerPayment.selectedItemId
        val selectedItem = binding.spinnerPayment.selectedItem as TypeSubscription

        if (selectedPaymentId.toInt() != 0) {
            viewModel.sendPayment(selectedItem.type)
        } else {
            Toast.makeText(
                context!!, "Debe seleccionar un método de pago|!!", Toast.LENGTH_SHORT
            ).show()
        }
    }
}
