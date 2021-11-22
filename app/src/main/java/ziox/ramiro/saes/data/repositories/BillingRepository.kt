package ziox.ramiro.saes.data.repositories

import android.app.Activity
import android.content.Context
import android.util.Log
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseData
import com.anjlab.android.iab.v3.PurchaseInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import ziox.ramiro.saes.R
import kotlin.coroutines.suspendCoroutine

interface BillingRepository {
    val purchase: SharedFlow<PurchaseData?>
    val billingError: SharedFlow<Throwable?>

    suspend fun purchaseDonation(productId: String)
    suspend fun hasDonated(): Boolean
    fun release()
}


class BillingGooglePayRepository(
    private val context: Context
): BillingRepository, BillingProcessor.IBillingHandler {
    private val client = BillingProcessor.newBillingProcessor(context, context.getString(R.string.billing_key), this)
    private val initializeFlow = MutableStateFlow<Boolean?>(null)

    init {
        client.initialize()
    }

    private val _purchase: MutableStateFlow<PurchaseData?> = MutableStateFlow(null)
    private val _billingError: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    override val purchase = _purchase.asSharedFlow()
    override val billingError = _billingError.asSharedFlow()

    override suspend fun purchaseDonation(productId: String) {
        client.purchase(context as Activity, productId)
    }

    override suspend fun hasDonated(): Boolean{
        if(!waitToInitialize()){
            return false
        }
        suspendCoroutine<Boolean> {
            client.loadOwnedPurchasesFromGoogleAsync(object : BillingProcessor.IPurchasesResponseListener{
                override fun onPurchasesSuccess() {
                    it.resumeWith(Result.success(true))
                }

                override fun onPurchasesError() {
                    it.resumeWith(Result.failure(Exception()))
                }
            })
        }
        return client.listOwnedProducts().isNotEmpty()
    }

    override fun release() {
        client.release()
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        _purchase.value = details?.purchaseData
    }


    override fun onPurchaseHistoryRestored() {
        Log.d("Billing", "Purchase History Restored")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        error?.printStackTrace()
        _billingError.value = error
        initializeFlow.tryEmit(false)
    }

    private suspend fun waitToInitialize(): Boolean = initializeFlow.value ?: initializeFlow.first { it != null } ?: false

    override fun onBillingInitialized() {
        Log.d("Billing", "Billing initialized")
        initializeFlow.tryEmit(true)
    }
}