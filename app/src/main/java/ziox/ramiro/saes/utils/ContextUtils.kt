package ziox.ramiro.saes.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import ziox.ramiro.saes.R

fun Context.haveDonated() : Boolean {
    val bp = BillingProcessor(
        this,
        this.resources.getString(R.string.billingKey),
        object : BillingProcessor.IBillingHandler{
            override fun onBillingInitialized() {}
            override fun onPurchaseHistoryRestored() {}
            override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
            override fun onBillingError(errorCode: Int, error: Throwable?) {}
        }
    )


    return bp.listOwnedProducts().isNotEmpty()
}

@Suppress("DEPRECATION")
fun Context.isNetworkAvailable() : Boolean{
    if(getPreference(this, "offline_mode", false)){
        return false
    }

    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            }
        }
    } else {
        try {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
        }
    }

    return false
}