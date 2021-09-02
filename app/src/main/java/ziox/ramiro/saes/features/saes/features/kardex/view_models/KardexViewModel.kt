package ziox.ramiro.saes.features.saes.features.kardex.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class KardexViewModel(
    private val kardexRepository: KardexRepository
) : ViewModel() {
    val kardexData = mutableStateOf<KardexData?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchKardexData()
        error.dismissAfterTimeout()
    }

    private fun fetchKardexData() = viewModelScope.launch {
        kardexData.value = null

        kotlin.runCatching {
            kardexRepository.getMyKardexData()
        }.onSuccess {
            kardexData.value = it
        }.onFailure {
            error.value = "Error al obtener el kardex"
        }
    }
}