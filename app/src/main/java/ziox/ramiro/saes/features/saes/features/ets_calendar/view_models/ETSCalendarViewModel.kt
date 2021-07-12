package ziox.ramiro.saes.features.saes.features.ets_calendar.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.FilterState
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarRepository

class ETSCalendarViewModel(
    private val etsCalendarRepository: ETSCalendarRepository
) : FilterViewModel() {
    override fun getFilterFields() = viewModelScope.launch {
        emitState(FilterState.FilterLoading())

        kotlin.runCatching {
            etsCalendarRepository.getFilters()
        }.onSuccess {
            emitState(FilterState.FilterComplete(it))
        }
    }

    override fun selectFilterField(itemId: String, newIndex: Int?) = viewModelScope.launch {
        kotlin.runCatching {
            etsCalendarRepository.setSelectFilter(itemId, newIndex)
        }.onSuccess {
            emitState(FilterState.FilterComplete(it))
            fetchETSCalendarEvents()
        }
    }

    fun fetchETSCalendarEvents() = viewModelScope.launch {

    }
}