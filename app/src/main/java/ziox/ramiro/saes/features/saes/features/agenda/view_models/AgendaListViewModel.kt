package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class AgendaListViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository
) : ViewModel() {
    val agendaList = mutableStateOf<List<AgendaCalendar>?>(null)
    val isAddingAgenda = mutableStateOf(false)
    val isRemovingAgenda = mutableStateOf<ArrayList<String>>(arrayListOf())
    val error = MutableStateFlow<String?>(null)

    init {
        fetchAgendas()
        error.dismissAfterTimeout()
    }

    private fun fetchAgendas() = viewModelScope.launch {
        kotlin.runCatching {
            agendaRepository.getCalendars().collect {
                agendaList.value = it
            }
        }.onFailure {
            it.printStackTrace()
            error.value = "Error al obtener la lista de agendas"
        }
    }

    fun addAgenda(agendaName: String) = viewModelScope.launch {
        isAddingAgenda.value = true
        kotlin.runCatching {
            agendaRepository.addCalendar(agendaName)
        }.onFailure {
            error.value = "Error al agregar la agenda"
        }

        isAddingAgenda.value = false
    }

    @Suppress("UNCHECKED_CAST")
    fun removeAgenda(calendarId: String) = viewModelScope.launch {
        isRemovingAgenda.value = (isRemovingAgenda.value.clone() as ArrayList<String>).also {
            it.add(calendarId)
        }
        kotlin.runCatching {
            agendaRepository.removeCalendar(calendarId)
        }.onFailure {
            error.value = "Error al eliminar la agenda"
            isRemovingAgenda.value = (isRemovingAgenda.value.clone() as ArrayList<String>).also {
                it.remove(calendarId)
            }
        }
    }
}