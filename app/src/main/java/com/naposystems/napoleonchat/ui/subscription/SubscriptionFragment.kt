package com.naposystems.napoleonchat.ui.subscription

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SubscriptionFragmentBinding
import com.naposystems.napoleonchat.model.typeSubscription.SubscriptionUser
import com.naposystems.napoleonchat.model.typeSubscription.TypeSubscription
import com.naposystems.napoleonchat.ui.cancelSubscription.CancelSubscriptionDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
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
    private var listTypeSubscription: List<TypeSubscription>? = null
    private var menuItem: MenuItem? = null
    private var subscriptionState: String = ""

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
        }

    private lateinit var billingClient: BillingClient

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
            inflater,
            R.layout.subscription_fragment,
            container,
            false
        )

        binding.checkBoxPaymentDescription.isChecked = false
        binding.imageButtonPaypal.isEnabled = false
        binding.checkBoxPaymentDescription.setOnCheckedChangeListener { _, _ ->
            enableButtonPaypal()
        }

        binding.spinnerPayment.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    enableButtonPaypal()
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    enableButtonPaypal()
                }
            }

        binding.imageButtonPaypal.setOnClickListener {
            binding.viewSwitcher.showNext()
            sendPayment()
        }

        startBillingClient()

        return binding.root
    }

    private fun startBillingClient() {

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here. 00sub
                    val skuList = ArrayList<String>()
                    skuList.add("00sub")
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

                    lifecycleScope.launch(Dispatchers.IO) {
                        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                            val responseCode = billingResult.responseCode
                            val debugMessage = billingResult.debugMessage
                            when (responseCode) {
                                BillingClient.BillingResponseCode.OK -> {
                                    Timber.i("onSkuDetailsResponse: $responseCode $debugMessage")
                                    if (skuDetailsList == null) {
                                        Timber.w("onSkuDetailsResponse: null SkuDetails list")
//                                        skusWithSkuDetails.postValue(emptyMap())
                                    } else
                                        Timber.i("onSkuDetailsResponse: count ${skuDetailsList.size}")
                                    /*skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                                        for (details in skuDetailsList) {
                                            put(details.sku, details)
                                        }
                                    }.also { postedValue ->
                                        Timber.i("onSkuDetailsResponse: count ${postedValue.size}")
                                    })*/
                                }
                                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                                BillingClient.BillingResponseCode.ERROR -> {
                                    Timber.e("onSkuDetailsResponse: $responseCode $debugMessage")
                                }
                                BillingClient.BillingResponseCode.USER_CANCELED,
                                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
                                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                                    // These response codes are not expected.
                                    Timber.wtf("onSkuDetailsResponse: $responseCode $debugMessage")
                                }
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingClient()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_subscription, menu)

        menuItem = menu.findItem(R.id.menu_item_cancel_subscription)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_cancel_subscription -> {
                val cancelSubscriptionDialogFragment =
                    CancelSubscriptionDialogFragment.newInstance()
                cancelSubscriptionDialogFragment.setListener(object :
                    CancelSubscriptionDialogFragment.Listener {
                    override fun subscriptionCancelledSuccessfully() {

                    }
                })
                cancelSubscriptionDialogFragment.show(childFragmentManager, "cancelSubscription")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.checkSubscription()
        viewModel.getTypeSubscription()

        viewModel.typeSubscription.observe(viewLifecycleOwner, Observer { listTypeSubscription ->
            binding.checkBoxPaymentDescription.isChecked = false
            if (listTypeSubscription.isNotEmpty()) {
                this.listTypeSubscription = listTypeSubscription
                val selectSubscription = getString(R.string.text_select_subscription)
                val newListSubscription = listTypeSubscription.toMutableList()

                newListSubscription.add(0, TypeSubscription(0, selectSubscription, 0, 0, 0.0))

                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.subscription_item,
                    R.id.textView_subscription_item,
                    newListSubscription
                )
                binding.spinnerPayment.adapter = adapter
                viewModel.getRemoteSubscription()
            }
        })

        viewModel.subscriptionUser.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                setSubscriptionUser(it)
            }
        })

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

        viewModel.subscriptionState.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                subscriptionState = it
                when (it) {
                    Constants.SubscriptionStatus.ACTIVE.state -> {
                        menuItem?.isVisible = true
                    }
                    else -> menuItem?.isVisible = false
                }
            }
        })
    }

    private fun enableButtonPaypal() {
        when (binding.spinnerPayment.selectedItemId) {
            null -> {
                binding.imageButtonPaypal.isEnabled = false
            }
            else -> {
                binding.imageButtonPaypal.isEnabled =
                    binding.spinnerPayment.selectedItemId.toInt() != 0 && binding.checkBoxPaymentDescription.isChecked
            }
        }
    }

    private fun setSubscriptionUser(subscriptionUser: SubscriptionUser) {
        binding.imageButtonPaypal.isEnabled = false
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val netDate: Date

        if (System.currentTimeMillis() > viewModel.getFreeTrial()) {
            if (subscriptionUser.dateExpires != 0L) {
                if (System.currentTimeMillis() > subscriptionUser.dateExpires) {
                    binding.textViewSubscriptionActual.text =
                        getString(R.string.text_expired_subscription)
                } else {
                    val daysMillis = subscriptionUser.dateExpires - System.currentTimeMillis()
                    val currentSubscription =
                        listTypeSubscription?.find { typeSubscription ->
                            typeSubscription.id == subscriptionUser.subscriptionId
                        }?.description
                    binding.textViewSubscriptionActual.text = getString(
                        R.string.text_subscription_and_time,
                        currentSubscription,
                        TimeUnit.MILLISECONDS.toDays(daysMillis)
                    )
                }
            } else {
                binding.textViewSubscriptionActual.text =
                    getString(R.string.text_free_trial_expired)
            }
            netDate = Date(subscriptionUser.dateExpires)
        } else {
            netDate = Date(viewModel.getFreeTrial())
            val daysMillis = viewModel.getFreeTrial() - System.currentTimeMillis()

            binding.textViewSubscriptionActual.text =
                getString(R.string.text_trial_period, TimeUnit.MILLISECONDS.toDays(daysMillis))
        }
        binding.textViewSubscriptionExpiration.text = sdf.format(netDate)
    }

    private fun sendPayment() {
        val selectedItem = binding.spinnerPayment.selectedItem as TypeSubscription
        viewModel.sendPayment(selectedItem.id)
    }
}
