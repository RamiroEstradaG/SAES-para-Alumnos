package ziox.ramiro.saes.features.saes.features.ets_calendar.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import ziox.ramiro.saes.features.saes.data.models.FilterState
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarRepository

class ETSCalendarViewModel(
    private val etsCalendarRepository: ETSCalendarRepository
) : FilterViewModel() {
    init {
        getFilterFields()
    }

    override fun getFilterFields() {
        viewModelScope.launch {
            emitState(FilterState.FilterLoading())

            kotlin.runCatching {
                etsCalendarRepository.getFilters()
            }.onSuccess {
                emitState(FilterState.FilterComplete(it))
                fetchETSCalendarEvents()
            }.onFailure {
                emitEvent(ETSCalendarEvent.Error("Error al obtener los filtros"))
            }
        }
    }

    override fun selectFilterField(itemId: String, newIndex: Int?) {
        viewModelScope.launch {
            emitState(FilterState.FilterLoading())

            kotlin.runCatching {
                etsCalendarRepository.setSelectFilter(itemId, newIndex)
            }.onSuccess {
                emitState(FilterState.FilterComplete(it))
                fetchETSCalendarEvents()
            }.onFailure {
                emitEvent(ETSCalendarEvent.Error("Error al seleccionar el campo"))
            }
        }
    }

    private suspend fun fetchETSCalendarEvents() {
        delay(500)
        emitState(ETSCalendarState.EventsLoading())

        kotlin.runCatching {
            etsCalendarRepository.getETSEvents()
        }.onSuccess {
            emitState(ETSCalendarState.EventsComplete(it))
        }.onFailure {
            emitEvent(ETSCalendarEvent.Error("Error al obtener el calendario de ETS"))
        }
    }
}