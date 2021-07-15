package ziox.ramiro.saes.features.saes.features.occupancy.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.FilterState
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyRepository

class OccupancyViewModel(
    private val occupancyRepository: OccupancyRepository
) : FilterViewModel() {
    override fun getFilterFields() = viewModelScope.launch {
        emitState(FilterState.FilterLoading())

        kotlin.runCatching {
            occupancyRepository.getFilters()
        }.onSuccess {
            emitState(FilterState.FilterComplete(it))
            fetchOccupancyList()
        }.onFailure {
            emitEvent(OccupancyEvent.Error("Error al obtener los filtros"))
        }
    }

    override fun selectFilterField(itemId: String, newIndex: Int?) = viewModelScope.launch {
        emitState(FilterState.FilterLoading())

        kotlin.runCatching {
            occupancyRepository.selectFilterField(itemId, newIndex)
        }.onSuccess {
            emitState(FilterState.FilterComplete(it))
            fetchOccupancyList()
        }.onFailure {
            emitEvent(OccupancyEvent.Error("Error al obtener los filtros"))
        }
    }

    private suspend fun fetchOccupancyList() {
        delay(500)
        emitState(OccupancyState.Loading())

        kotlin.runCatching {
            occupancyRepository.getOccupancyData()
        }.onSuccess {
            emitState(OccupancyState.Complete(it))
        }.onFailure {
            emitEvent(OccupancyEvent.Error("Error al obtener la ocupabilidad"))
        }
    }
}