package ziox.ramiro.saes.features.saes.features.occupancy.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.occupancy.data.models.ClassOccupancy
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class OccupancyViewModel(
    private val occupancyRepository: OccupancyRepository
) : FilterViewModel() {
    val occupancyList = mutableStateOf<List<ClassOccupancy>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        getFilterFields()
        error.dismissAfterTimeout()
    }

    override fun getFilterFields() = viewModelScope.launch {
        filterFields.value = null

        kotlin.runCatching {
            occupancyRepository.getFilters()
        }.onSuccess {
            filterFields.value = it
            filterFieldsComplete.value = it
            fetchOccupancyList()
        }.onFailure {
            filterError.value = "Error al obtener los filtros"
        }
    }

    override fun selectSelect(itemId: String, newIndex: Int?) = viewModelScope.launch {
        filterFields.value = null

        kotlin.runCatching {
            occupancyRepository.selectSelect(itemId, newIndex)
        }.onSuccess {
            filterFields.value = it
            filterFieldsComplete.value = it
            fetchOccupancyList()
        }.onFailure {
            filterError.value = "Error al obtener los filtros"
        }
    }

    override fun selectRadioGroup(fieldId: String){}

    private suspend fun fetchOccupancyList() {
        occupancyList.value = null

        kotlin.runCatching {
            occupancyRepository.getOccupancyData()
        }.onSuccess {
            occupancyList.value = it
        }.onFailure {
            error.value = "Error al obtener la ocupabilidad"
        }
    }
}