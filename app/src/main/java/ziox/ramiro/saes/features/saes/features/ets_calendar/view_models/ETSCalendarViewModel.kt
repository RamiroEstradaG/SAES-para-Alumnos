package ziox.ramiro.saes.features.saes.features.ets_calendar.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarItem
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarRepository

class ETSCalendarViewModel(
    private val etsCalendarRepository: ETSCalendarRepository
) : FilterViewModel() {
    val etsCalendar = mutableStateOf<List<ETSCalendarItem>?>(null)
    val error = mutableStateOf<String?>(null)

    init {
        getFilterFields()
    }

    override fun getFilterFields() {
        viewModelScope.launch {
            filterFields.value = null

            kotlin.runCatching {
                etsCalendarRepository.getFilters()
            }.onSuccess {
                filterFields.value = it
                filterFieldsComplete.value = it
                fetchETSCalendarEvents()
            }.onFailure {
                filterError.value = "Error al obtener los filtros"
            }
        }
    }

    override fun selectSelect(itemId: String, newIndex: Int?) {
        viewModelScope.launch {
            filterFields.value = null

            kotlin.runCatching {
                etsCalendarRepository.selectSelect(itemId, newIndex)
            }.onSuccess {
                filterFields.value = it
                filterFieldsComplete.value = it
                fetchETSCalendarEvents()
            }.onFailure {
                filterError.value = "Error al seleccionar el campo"
            }
        }
    }

    override fun selectRadioGroup(fieldId: String) {}

    private suspend fun fetchETSCalendarEvents() {
        etsCalendar.value = null

        kotlin.runCatching {
            etsCalendarRepository.getETSEvents()
        }.onSuccess {
            etsCalendar.value = it
        }.onFailure {
            error.value = "Error al obtener el calendario de ETS"
        }
    }
}