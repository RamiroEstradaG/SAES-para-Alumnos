package ziox.ramiro.saes.view_models

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.BillingRepository
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor (
    private val billingRepository: BillingRepository,
): ViewModel() {
    val error = MutableStateFlow<String?>(null)
    val productList = billingRepository.productList
    val hasDonated = billingRepository.hasDonated

    fun refetch() = viewModelScope.launch {
        kotlin.runCatching {
            billingRepository.refetch()
        }.onFailure {
            error.value = "Error al comprobar si ha donado"
        }
    }

    fun purchaseProduct(product: ProductDetails, activity: Activity) = viewModelScope.launch {
        kotlin.runCatching {
            billingRepository.purchaseDonation(product, activity)
        }.onFailure {
            it.printStackTrace()
            error.value = "Error al lanzar el proceso de compra"
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.release()
    }
}