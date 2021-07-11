package ziox.ramiro.saes.features.saes.features.kardex.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository

class KardexViewModel(
    private val kardexRepository: KardexRepository
) : BaseViewModel<KardexState, KardexEvent>() {
    init {
        fetchKardexData()
    }

    private fun fetchKardexData() = viewModelScope.launch {
        emitState(KardexState.Loading())

        kotlin.runCatching {
            kardexRepository.getMyKardexData()
        }.onSuccess {
            emitState(KardexState.Complete(it))
        }.onFailure {
            emitEvent(KardexEvent.Error("Error al obtener el kardex"))
        }
    }
}