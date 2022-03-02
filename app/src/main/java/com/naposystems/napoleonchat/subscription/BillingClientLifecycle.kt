package com.naposystems.napoleonchat.subscription

import android.app.Activity
import androidx.lifecycle.*
import com.android.billingclient.api.*
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BillingClientLifecycle @Inject constructor(private val app: NapoleonApplication) :
    LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    SkuDetailsResponseListener {

    private lateinit var billingClient: BillingClient

    private val _skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    val skusWithSkuDetails: LiveData<Map<String, SkuDetails>>
        get() = _skusWithSkuDetails

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>>
        get() = _purchases

    private val _purchaseUpdateListener = MutableLiveData<List<Purchase>>()
    val purchaseUpdateListener: LiveData<List<Purchase>>
        get() = _purchaseUpdateListener

    private val _purchaseError = MutableLiveData<Int>()
    val purchaseError: LiveData<Int>
        get() = _purchaseError

    private val _purchasesHistory = MutableLiveData<List<PurchaseHistoryRecord>>()
    val purchasesHistory: LiveData<List<PurchaseHistoryRecord>>
        get() = _purchasesHistory

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        Timber.d("ON_CREATE")
        billingClient = BillingClient.newBuilder(app.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            Timber.d("BillingClient: Start connection...")
            billingClient.startConnection(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        Timber.d("ON_DESTROY")
        _purchaseUpdateListener.postValue(null)
        _purchases.postValue(null)
        _purchaseError.postValue(null)
        _purchasesHistory.postValue(null)
        _skusWithSkuDetails.postValue(null)
        if (billingClient.isReady) {
            Timber.d("BillingClient can only be used once -- closing connection")
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            billingClient.endConnection()
        }
    }

    /**
     * In order to make purchasese, you need the [SkuDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in [onSkuDetailsResponse].
     */
    private fun querySkuDetails() {
        Timber.d("querySkuDetails")
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.SUBS)
            .setSkusList(
                listOf(
                    Constants.SkuSubscriptions.QUARTERLY.sku,
                    Constants.SkuSubscriptions.SEMIANNUAL.sku,
                    Constants.SkuSubscriptions.YEARLY.sku
                )
            )
            .build()
        params?.let { skuDetailsParams ->
            Timber.i("querySkuDetailsAsync")
            billingClient.querySkuDetailsAsync(skuDetailsParams, this)
        }
    }

    /**
     * Launching the billing flow.
     *
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        val sku = params.sku
        val oldSku = params.oldSku
        Timber.i("launchBillingFlow: sku: $sku, oldSku: $oldSku")
        if (!billingClient.isReady) {
            Timber.e("launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient.launchBillingFlow(activity, params)
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode
    }

    /**
     * Receives the result from [querySkuDetails].
     *
     * Store the SkuDetails and post them in the [skusWithSkuDetails]. This allows other parts
     * of the app to use the [SkuDetails] to show SKU information and make purchases.
     */
    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Timber.i("onSkuDetailsResponse: $responseCode $debugMessage")
                if (skuDetailsList == null) {
                    Timber.w("onSkuDetailsResponse: null SkuDetails list")
                    _skusWithSkuDetails.postValue(emptyMap())
                } else
                    _skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                        for (details in skuDetailsList) {
                            put(details.sku, details)
                        }
                    }.also { postedValue ->
                        Timber.i("onSkuDetailsResponse: count ${postedValue.size}")
                    })
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

    /**
     * Query Google Play Billing for existing purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    fun queryPurchases() {
        if (!billingClient.isReady) {
            Timber.e("queryPurchases: BillingClient is not ready")
        }
        Timber.d("queryPurchases: SUBS")
        val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result == null) {
            Timber.i("queryPurchases: null purchase result")
            processPurchases(null)
        } else {
            if (result.purchasesList == null) {
                Timber.i("queryPurchases: null purchase list")
                processPurchases(null)
            } else {
                Timber.d("queryPurchases: ${result.responseCode}, ${result.purchasesList?.size}")
                result.purchasesList?.forEach {
                    Timber.d("queryPurchases: ${it.sku}, ${it.purchaseTime}, ${it.purchaseState}")
                }
                processPurchases(result.purchasesList)
            }
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        Timber.d("processPurchases: ${purchasesList?.size} purchase(s)")
        if (isUnchangedPurchaseList(purchasesList)) {
            Timber.d("processPurchases: Purchase list has not changed")
            return
        }
        _purchases.postValue(purchasesList)
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>?): Boolean {
        // TODO: Optimize to avoid updates with identical data.
        return false
    }

    fun queryPurchasesHistory() {
        if (!billingClient.isReady) {
            Timber.e("queryPurchases: BillingClient is not ready")
        }
        Timber.d("queryPurchasesHistory: SUBS")
        billingClient.queryPurchaseHistoryAsync(
            BillingClient.SkuType.SUBS
        ) { _, purchasesHistoryList ->
            _purchasesHistory.postValue(
                purchasesHistoryList
            )
        }
    }

    fun acknowledged(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
            GlobalScope.launch(Dispatchers.IO) {
                val ackPurchaseResult =
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                Timber.d("Billing ${ackPurchaseResult.responseCode}, ${ackPurchaseResult.debugMessage}")
            }
        }
    }

    //region Implementation PurchasesUpdatedListener
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("onPurchasesUpdated: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Timber.d("onPurchasesUpdated: null purchase list")
                    _purchaseUpdateListener.postValue(emptyList())
                } else {
                    _purchaseUpdateListener.postValue(purchases)
                }
            }
            else -> _purchaseError.value = responseCode
        }
    }
    //endregion

    //region Implementation BillingClientStateListener
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.d("onBillingSetupFinished: $responseCode $debugMessage")
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // The billing client is ready. You can query purchases here.
            querySkuDetails()
            queryPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {

    }
    //endregion
}