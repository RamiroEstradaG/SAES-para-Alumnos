package ziox.ramiro.saes.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.BillingRepository
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor (
    private val billingRepository: BillingRepository,
): ViewModel() {
    val hasDonated = mutableStateOf<Boolean?>(null)
    val error = MutableStateFlow<String?>(null)
    val productList = billingRepository.productList

    init {
        hasDonated()
    }

    fun hasDonated() = viewModelScope.launch {
        kotlin.runCatching {
            billingRepository.hasDonated()
        }.onSuccess {
            hasDonated.value = it
        }.onFailure {
            error.value = "Error al obtener información sobre la donación"
        }
    }

    fun purchaceProduct(productId: String) = viewModelScope.launch {
        kotlin.runCatching {
            billingRepository.purchaseDonation(productId)
        }.onFailure {
            error.value = "Error al lanzar el proceso de compra"
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.release()
    }
}