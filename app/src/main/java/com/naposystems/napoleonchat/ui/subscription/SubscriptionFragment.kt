package com.naposystems.napoleonchat.ui.subscription

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SubscriptionFragmentBinding
import com.naposystems.napoleonchat.model.typeSubscription.TypeSubscription
import com.naposystems.napoleonchat.subscription.BillingClientLifecycle
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.dialog.cancelSubscription.CancelSubscriptionDialogFragment
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.CreateSuscriptionDTO
import com.naposystems.napoleonchat.ui.subscription.adapter.SkuDetailsAdapter
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SubscriptionFragment : BaseFragment() {

    companion object {
        fun newInstance() = SubscriptionFragment()
    }

    private var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }
    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

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

        binding.buttonBuyPaymentsway.setOnClickListener {
                val userId = sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_USER_ID, "")
                val url = getString(R.string.buy_subscription_url).plus(userId)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
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

     //   viewModel.checkSubscription()
        //
        //  viewModel.getTypeSubscription()

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
                billingClientLifecycle.queryPurchasesHistory()
            }
        })

        billingClientLifecycle.purchasesHistory.observe(
            viewLifecycleOwner,
            Observer { purchasesHistory ->
                purchasesHistory?.let {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val netDate: Date
                    val freeTrial = viewModel.getFreeTrial()
                    Timber.d("freeTrial: $freeTrial")
                    if (System.currentTimeMillis() > freeTrial) {
                        if (purchasesHistory.isNotEmpty()) {
                            val dateExpireSubscriptionMillis =
                                getDataSubscription(purchasesHistory)
                            val lastPurchase = purchasesHistory[0]
                            val time = System.currentTimeMillis()
                            if (time > dateExpireSubscriptionMillis) {
                                binding.textViewSubscriptionActual.text =
                                    getString(R.string.text_subscription_expired)
                            } else {
                                binding.textViewSubscriptionActual.text = when (lastPurchase.sku) {
                                    Constants.SkuSubscriptions.QUARTERLY.sku -> getString(R.string.text_subscription_quarterly)
                                    Constants.SkuSubscriptions.SEMIANNUAL.sku -> getString(R.string.text_subscription_semiannual)
                                    else -> getString(R.string.text_subscription_yearly)
                                }
                            }
                            netDate = Date(dateExpireSubscriptionMillis)
                        } else {
                            binding.textViewSubscriptionActual.text =
                                getString(R.string.text_free_trial_expired)
                            netDate = Date(freeTrial)
                        }
                    } else {
                        netDate = Date(freeTrial)
                        val daysMillis = viewModel.getFreeTrial() - System.currentTimeMillis()

                        binding.textViewSubscriptionActual.text =
                            getString(
                                R.string.text_trial_period,
                                TimeUnit.MILLISECONDS.toDays(daysMillis)
                            )
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
                snackbarUtils.showSnackbar {}
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
            snackbarUtils.showSnackbar {}
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

    private fun getDataSubscription(purchasesHistory: List<PurchaseHistoryRecord>): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchasesHistory[0].purchaseTime
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val lastPurchase = purchasesHistory[0]


        when (lastPurchase.sku) {
            Constants.SkuSubscriptions.QUARTERLY.sku ->
                calendar.add(Calendar.MONTH, Constants.SubscriptionsTimeType.QUARTERLY.subscription)
            Constants.SkuSubscriptions.SEMIANNUAL.sku ->
                calendar.add(
                    Calendar.MONTH,
                    Constants.SubscriptionsTimeType.SEMIANNUAL.subscription
                )
            Constants.SkuSubscriptions.YEARLY.sku ->
                calendar.add(Calendar.YEAR, Constants.SubscriptionsTimeType.YEARLY.subscription)

        }

        val dateExpireSubscription = sdf.parse(sdf.format(calendar.time))
        return dateExpireSubscription!!.time
    }

    private fun setPriceTextButton() {
        val skuDetailsSelected = binding.spinnerPayment.selectedItem as SkuDetails
        binding.buttonBuySubscription.text = getString(
            R.string.text_purchase,
            skuDetailsSelected.price,
            skuDetailsSelected.priceCurrencyCode
        )
        binding.buttonBuyPaymentsway.text = getString(R.string.text_payments_way)
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
            Timber.d("jennyfer Register purchase with sku: $sku, token: $purchaseToken")
            Utils.showSimpleSnackbar(
                binding.coordinator,
                getString(R.string.text_subscription_successfully),
                3
            )
            if (binding.viewSwitcher.nextView.id == binding.buttonBuySubscription.id) {
                binding.viewSwitcher.showNext()
            }

            binding.checkBoxPaymentDescription.isChecked = false
            binding.buttonBuySubscription.visibility = View.GONE
            billingClientLifecycle.queryPurchases()
            billingClientLifecycle.acknowledged(purchase)

            val user_id =sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_USER_ID, "")
            val createSuscriptionDTO = CreateSuscriptionDTO(
                user_id = user_id,
                subscription_id = "6"
            )
            
            viewModel.createSubscription(createSuscriptionDTO)
            saveSubscription(SubscriptionStatus.ACTIVE,user_id)
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionStatus = SubscriptionStatus.valueOf(
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.SubscriptionStatus,
                SubscriptionStatus.ACTIVE.name
            )
        )
        observeSubscription()
    }

    private fun saveSubscription(
        subscriptionStatus: SubscriptionStatus,
        userId: String
    ) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.SubscriptionStatus,
            subscriptionStatus.name
        )
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_USER_ID,
            userId
        )

        RxBus.publish(RxEvent.SubscriptionStatusEvent(subscriptionStatus))
       // subscriptionNotificationWork()
    }


    private fun observeSubscription() {
        val disposableSubscriptionStatus = RxBus.listen(RxEvent.SubscriptionStatusEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                subscriptionStatus = it.status
            }
        disposable.add(disposableSubscriptionStatus)
    }
}
