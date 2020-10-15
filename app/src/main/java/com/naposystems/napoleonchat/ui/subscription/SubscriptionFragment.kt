package com.naposystems.napoleonchat.ui.subscription

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SubscriptionFragmentBinding
import com.naposystems.napoleonchat.model.typeSubscription.TypeSubscription
import com.naposystems.napoleonchat.subscription.BillingClientLifecycle
import com.naposystems.napoleonchat.ui.cancelSubscription.CancelSubscriptionDialogFragment
import com.naposystems.napoleonchat.ui.subscription.adapter.SkuDetailsAdapter
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
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

    @Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle

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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(billingClientLifecycle)
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
        binding.buttonBuySubscription.isEnabled = false
        binding.checkBoxPaymentDescription.setOnCheckedChangeListener { _, _ ->
            enableButtonPaypal()
        }

        binding.spinnerPayment.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    setPriceTextButton()
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setPriceTextButton()
                }
            }

        binding.buttonBuySubscription.setOnClickListener {
            binding.viewSwitcher.showNext()
            sendPayment()
        }

        return binding.root
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

        billingClientLifecycle.skusWithSkuDetails.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.checkBoxPaymentDescription.isChecked = false

                val skuDetailsList = it.map { map -> map.value }

                skuDetailsList.sortedBy { skuDetails -> skuDetails.priceAmountMicros }

                val adapter =
                    SkuDetailsAdapter(requireContext(), R.layout.subscription_item, skuDetailsList)
                binding.spinnerPayment.adapter = adapter
            }
        })

        billingClientLifecycle.purchases.observe(viewLifecycleOwner, Observer { purchasesList ->
            purchasesList?.let {
                if (purchasesList.isEmpty()) {
                    billingClientLifecycle.queryPurchasesHistory()
                } else {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val netDate: Date

                    val lastPurchase = purchasesList[0]
                    binding.textViewSubscriptionActual.text = when (lastPurchase.sku) {
                        Constants.SkuSubscriptions.MONTHLY.sku -> getString(R.string.text_subscription_monthly)
                        Constants.SkuSubscriptions.SEMIANNUAL.sku -> getString(R.string.text_subscription_semiannual)
                        else -> getString(R.string.text_subscription_yearly)
                    }
                    netDate = Date(lastPurchase.purchaseTime)
                    binding.textViewSubscriptionExpiration.text = sdf.format(netDate)
                }
            }
        })

        billingClientLifecycle.purchasesHistory.observe(
            viewLifecycleOwner,
            Observer { purchasesHistory ->
                purchasesHistory?.let {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val netDate: Date
                    val freeTrialTimeStamp = viewModel.getFreeTrial()
                    if (purchasesHistory.isEmpty()) {
                        if (System.currentTimeMillis() > freeTrialTimeStamp) {
                            binding.textViewSubscriptionActual.text =
                                getString(R.string.text_free_trial_expired)
                            netDate = Date(freeTrialTimeStamp)
                        } else {
                            netDate = Date(freeTrialTimeStamp)
                            val daysMillis = viewModel.getFreeTrial() - System.currentTimeMillis()

                            binding.textViewSubscriptionActual.text =
                                getString(
                                    R.string.text_trial_period,
                                    TimeUnit.MILLISECONDS.toDays(daysMillis)
                                )
                        }
                    } else {
                        // Get last purchase
                        val lastPurchase = purchasesHistory[0]
                        binding.textViewSubscriptionActual.text = when (lastPurchase.sku) {
                            Constants.SkuSubscriptions.MONTHLY.sku -> getString(R.string.text_subscription_monthly)
                            Constants.SkuSubscriptions.SEMIANNUAL.sku -> getString(R.string.text_subscription_semiannual)
                            else -> getString(R.string.text_subscription_yearly)
                        }
                        netDate = Date(lastPurchase.purchaseTime)
                        purchasesHistory.forEach {
                            Timber.d("purchasesHistory ${it.sku}, ${it.purchaseTime}")
                        }
                    }
                    binding.textViewSubscriptionExpiration.text = sdf.format(netDate)
                }
            })

        billingClientLifecycle.purchaseUpdateListener.observe(
            viewLifecycleOwner,
            Observer { purchaseList ->
                purchaseList?.let {
                    registerPurchase(purchaseList)
                }
            })

        billingClientLifecycle.purchaseError.observe(viewLifecycleOwner, Observer { responseCode ->
            responseCode?.let {
                when (responseCode) {
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        if (binding.viewSwitcher.nextView.id == binding.buttonBuySubscription.id) {
                            binding.viewSwitcher.showNext()
                        }
                        Utils.showSimpleSnackbar(
                            binding.coordinator,
                            getString(R.string.text_operation_cancelled),
                            5
                        )
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        if (binding.viewSwitcher.nextView.id == binding.buttonBuySubscription.id) {
                            binding.viewSwitcher.showNext()
                        }
                        Utils.showSimpleSnackbar(
                            binding.coordinator,
                            getString(R.string.text_subscription_already_owned),
                            5
                        )
                    }
                    else -> {
                        if (binding.viewSwitcher.nextView.id == binding.buttonBuySubscription.id) {
                            binding.viewSwitcher.showNext()
                        }
                        Utils.showSimpleSnackbar(
                            binding.coordinator,
                            getString(R.string.text_subscription_error),
                            3
                        )
                    }
                }
            }
        })

        /*viewModel.typeSubscription.observe(viewLifecycleOwner, Observer { listTypeSubscription ->
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
        })*/

        viewModel.getTypeSubscriptionError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar{}
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
            snackbarUtils.showSnackbar{}
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

    private fun setPriceTextButton() {
        val skuDetailsSelected = binding.spinnerPayment.selectedItem as SkuDetails
        binding.buttonBuySubscription.text = getString(
            R.string.text_purchase,
            skuDetailsSelected.price,
            skuDetailsSelected.priceCurrencyCode
        )
    }

    private fun enableButtonPaypal() {
        when (binding.spinnerPayment.selectedItemId) {
            null -> {
                binding.buttonBuySubscription.isEnabled = false
            }
            else -> {
                binding.buttonBuySubscription.isEnabled =
                    binding.checkBoxPaymentDescription.isChecked
            }
        }
    }

    /*
    private fun setSubscriptionUser(purchasesList: List<Purchase>) {
        binding.buttonBuySubscription.isEnabled = false
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
    }*/

    private fun sendPayment() {
        /*val selectedItem = binding.spinnerPayment.selectedItem as TypeSubscription
        viewModel.sendPayment(selectedItem.id)*/
        val skuDetailSelected = binding.spinnerPayment.selectedItem as SkuDetails

        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetailSelected)
            .build()
        billingClientLifecycle.launchBillingFlow(requireActivity(), flowParams)
    }

    private fun registerPurchase(purchaseList: List<Purchase>) {
        for (purchase in purchaseList) {
            val sku = purchase.sku
            val purchaseToken = purchase.purchaseToken
            Timber.d("Register purchase with sku: $sku, token: $purchaseToken")
            Utils.showSimpleSnackbar(binding.coordinator, getString(R.string.text_subscription_successfully), 3)
            if (binding.viewSwitcher.nextView.id == binding.buttonBuySubscription.id) {
                binding.viewSwitcher.showNext()
            }
            binding.checkBoxPaymentDescription.isChecked = false
            billingClientLifecycle.queryPurchases()
            /*subscriptionViewModel.registerSubscription(
                sku = sku,
                purchaseToken = purchaseToken
            )*/
        }
    }
}
