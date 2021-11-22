package ziox.ramiro.saes.features.saes.features.school_schedule.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class SchoolScheduleViewModel @Inject constructor(
    private val schoolScheduleRepository: SchoolScheduleRepository
) : FilterViewModel() {
    val schoolSchedule = mutableStateOf<List<ClassSchedule>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        getFilterFields()
        error.dismissAfterTimeout()
    }

    private suspend fun fetchSchoolSchedule() {
        schoolSchedule.value = null

        kotlin.runCatching {
            schoolScheduleRepository.getSchoolSchedule()
        }.onSuccess {
            schoolSchedule.value = it
        }.onFailure {
            error.value = "Error al obtener los horarios de la escuela"
        }
    }

    override fun getFilterFields() = viewModelScope.launch {
        filterFields.value = null

        kotlin.runCatching {
            schoolScheduleRepository.getFilters()
        }.onSuccess {
            filterFields.value = it
            filterFieldsComplete.value = it
            fetchSchoolSchedule()
        }.onFailure {
            filterError.value = "Error al obtener los filtros"
        }
    }

    override fun selectSelect(itemId: String, newIndex: Int?) = viewModelScope.launch {
        filterFields.value = null

        kotlin.runCatching {
            schoolScheduleRepository.selectSelect(itemId, newIndex)
        }.onSuccess {
            filterFields.value = it
            filterFieldsComplete.value = it
            fetchSchoolSchedule()
        }.onFailure {
            filterError.value = "Error al obtener los filtros"
        }
    }

    override fun selectRadioGroup(fieldId: String) = viewModelScope.launch {
        filterFields.value = null

        kotlin.runCatching {
            schoolScheduleRepository.selectRadioGroup(fieldId)
        }.onSuccess {
            filterFields.value = it
            filterFieldsComplete.value = it
            fetchSchoolSchedule()
        }.onFailure {
            filterError.value = "Error al obtener los filtros"
        }
    }

}