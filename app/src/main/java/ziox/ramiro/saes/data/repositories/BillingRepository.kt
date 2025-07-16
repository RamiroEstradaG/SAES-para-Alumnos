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
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull

interface BillingRepository {
    val productList: SharedFlow<List<ProductDetails>?>
    val purchase: SharedFlow<List<Purchase?>?>
    val billingError: SharedFlow<Throwable?>

    suspend fun purchaseDonation(productId: String)
    suspend fun hasDonated(): Boolean
    fun release()
}


class BillingGooglePayRepository(
    private val context: Context
): BillingRepository, BillingClientStateListener, PurchasesUpdatedListener {
    private val _productList: MutableSharedFlow<List<ProductDetails>?> = MutableStateFlow(null)
    override val productList = _productList.asSharedFlow()
    private val client = BillingClient.newBuilder(context).enablePendingPurchases(
        PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
    ).setListener(this).build()
    private val initializeFlow = MutableStateFlow(false)
    private val _purchase: MutableStateFlow<List<Purchase?>?> = MutableStateFlow(null)
    private val _billingError: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    override val purchase = _purchase.asSharedFlow()
    override val billingError = _billingError.asSharedFlow()

    init {
        client.startConnection(this)
    }

    override suspend fun purchaseDonation(productId: String) {
        val product = productList.lastOrNull()?.find {
            it.productId == productId
        } ?: run {
            Log.e("Billing", "Product not found: $productId")
            throw IllegalArgumentException("Product not found: $productId")
        }

        client.launchBillingFlow(
            context as Activity,
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

    override suspend fun hasDonated(): Boolean{
        waitToInitialize()
        return client.queryPurchasesAsync(
            QueryPurchasesParams
                .newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ).purchasesList.also {
            Log.d("Billing", "Purchases found: ${it.size}")
            it.forEach { purchase ->
                Log.d("Billing", "Purchase: ${purchase.orderId}")
            }
        }.isNotEmpty()
    }

    override fun release() {
        client.endConnection()
    }

    private suspend fun waitToInitialize() = if(initializeFlow.value){
        true
    }else{
        initializeFlow.first { it }
    }

    override fun onBillingServiceDisconnected() {
        Log.e("Billing", "Billing service disconnected")
        client.startConnection(this)
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        Log.d("Billing", "Billing initialized")
        initializeFlow.tryEmit(true)

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
        ){
            result, products ->
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
            _purchase.value = purchases
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("Billing", "Error purchasing: ${result.debugMessage}")
            _billingError.value = Exception(result.debugMessage)
        }
    }
}