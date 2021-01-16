package ziox.ramiro.saes.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import ziox.ramiro.saes.R

fun Context.haveDonated() : Boolean {
    val billingProcessor = BillingProcessor(
        this,
        this.resources.getString(R.string.billingKey),
        object : BillingProcessor.IBillingHandler{
            override fun onBillingInitialized() {}
            override fun onPurchaseHistoryRestored() {}
            override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
            override fun onBillingError(errorCode: Int, error: Throwable?) {}
        }
    )


    return billingProcessor.listOwnedProducts().isNotEmpty()
}


fun Context.isNetworkAvailable() : Boolean{
    if(getPreference(this, "offline_mode", false)){
        return false
    }

    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        }
    }

    return false
}

/**
 * Obtiene las propiedades de la pantalla actual del dispositivo
 * @return Un par, siendo el primero el ancho (width) y el segundo el alto (height)
 */
@Suppress("DEPRECATION")
fun Context.getWindowMetrics() : Pair<Int, Int> {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        val window = (this as Activity).windowManager.currentWindowMetrics.bounds
        Pair(window.width(), window.height())
    }else{
        val window = (this as Activity).windowManager.defaultDisplay
        Pair(window.width, window.height)
    }
}