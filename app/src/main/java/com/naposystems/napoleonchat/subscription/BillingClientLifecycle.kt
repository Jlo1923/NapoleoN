package com.naposystems.napoleonchat.subscription

import android.app.Activity
import android.util.Log
import androidx.lifecycle.*
import com.android.billingclient.api.*
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber
import javax.inject.Inject

class BillingClientLifecycle @Inject constructor(private val app: NapoleonApplication) :
    LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener,
    SkuDetailsResponseListener {

    private lateinit var billingClient: BillingClient

    private val _skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    val skusWithSkuDetails: LiveData<Map<String, SkuDetails>>
        get() = _skusWithSkuDetails

    private val _purchaseUpdateListener = MutableLiveData<List<Purchase>>()
    val purchaseUpdateListener: LiveData<List<Purchase>>
        get() = _purchaseUpdateListener

    private val _purchaseError = MutableLiveData<Int>()
    val purchaseError: LiveData<Int>
        get() = _purchaseError

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
                    Constants.SkuSubscriptions.MONTHLY.sku,
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
//            queryPurchases()
        }
    }

    override fun onBillingServiceDisconnected() {

    }
    //endregion
}