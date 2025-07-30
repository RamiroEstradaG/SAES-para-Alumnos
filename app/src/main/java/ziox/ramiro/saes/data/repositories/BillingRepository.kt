package ziox.ramiro.saes.data.repositories

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

interface BillingRepository {
    val productList: Flow<List<ProductDetails>?>
    val billingError: Flow<Throwable?>
    val purchases: Flow<List<Purchase?>?>
    val hasDonated: Flow<Boolean>

    fun purchaseDonation(product: ProductDetails, activity: Activity)
    fun refetch()
    fun release()
}


class BillingGooglePayRepository(
    private val context: Context
) : BillingRepository, BillingClientStateListener, PurchasesUpdatedListener {
    private var client = BillingClient.newBuilder(context).enablePendingPurchases(
        PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
    ).setListener(this).build()
    private val _billingError = MutableStateFlow<Throwable?>(null)
    override val billingError = _billingError.asSharedFlow()
    private val _purchases = MutableStateFlow<List<Purchase?>?>(null)
    override val purchases = _purchases.asSharedFlow()
    private val _hasDonated = MutableStateFlow(false)
    override val hasDonated = _hasDonated.asSharedFlow()
    private val _productList = MutableStateFlow<List<ProductDetails>?>(null)
    override val productList = _productList.asSharedFlow()

    var isLoading = false


    init {
        isLoading = true
        client.startConnection(this)
    }

    override fun purchaseDonation(product: ProductDetails, activity: Activity) {
        client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .build()
                    )
                )
                .build()
        )
    }

    override fun refetch() {
        if (client.connectionState == BillingClient.ConnectionState.CONNECTED) {
            Log.d("Billing", "Refetching purchases")
            fetchPurchases()
        } else if (!isLoading) {
            Log.w("Billing", "Billing client is not ready, starting connection")
            isLoading = true
            client.endConnection()
            client = BillingClient.newBuilder(context).enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            ).setListener(this).build()
            client.startConnection(this)
        }
    }

    private fun fetchPurchases() {
        client.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("Billing", "Fetched purchases: ${purchases.size}")
                _purchases.tryEmit(purchases)
                _hasDonated.tryEmit(purchases.isNotEmpty())
            } else {
                Log.e("Billing", "Error fetching purchases: ${result.debugMessage}")
                _billingError.value = Exception(result.debugMessage)
            }
        }
    }

    override fun release() {
        client.endConnection()
    }

    override fun onBillingServiceDisconnected() {
        Log.e("Billing", "Billing service disconnected")
        client.startConnection(this)
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        isLoading = false
        Log.d("Billing", "Billing initialized")

        fetchPurchases()

        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("01_saes_donation_20")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("02_saes_donation_50")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    )
                )
                .build()
        ) { result, products ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _productList.tryEmit(products.productDetailsList)
            } else {
                Log.e("Billing", "Error loading products: ${result.debugMessage}")
                _billingError.value = Exception(result.debugMessage)
            }
        }


    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: List<Purchase?>?
    ) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _purchases.tryEmit(purchases)
            _hasDonated.tryEmit(purchases.isNotEmpty())
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("Billing", "Error purchasing: ${result.debugMessage}")
            _billingError.value = Exception(result.debugMessage)
        }
    }
}