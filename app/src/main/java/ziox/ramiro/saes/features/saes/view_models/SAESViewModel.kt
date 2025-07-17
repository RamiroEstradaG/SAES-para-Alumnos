package ziox.ramiro.saes.features.saes.view_models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.utils.runOnDefaultThread
import javax.inject.Inject

@HiltViewModel
class SAESViewModel @Inject constructor(
    localAppDatabase: LocalAppDatabase
) : ViewModel() {
    companion object {
        val SECTION_INITIAL = MenuSection.HOME
    }

    private val historyRoomRepository = localAppDatabase.historyRepository()
    private val _currentSection = MutableStateFlow(SECTION_INITIAL)
    private val history = arrayListOf(SECTION_INITIAL)
    val currentSection = _currentSection.asSharedFlow()

    fun changeSection(newSection: MenuSection) = viewModelScope.launch {
        history.add(newSection)
        runOnDefaultThread {
            val lastItem = historyRoomRepository.getLastItem()

            if(newSection != MenuSection.HOME && newSection != lastItem?.section){
                historyRoomRepository.addItem(HistoryItem(section = newSection))
            }
        }

        _currentSection.emit(newSection)
    }

    fun goBack() = viewModelScope.launch {
        history.removeLastOrNull()
        _currentSection.emit(history.last())
    }

    fun canGoBack(): Boolean = history.size > 1
}


enum class MenuSection(
    val sectionName: String,
    val icon: ImageVector,
    val supportsOfflineMode: Boolean = false
){
    HOME("Inicio", Icons.Rounded.Home, true),
    SCHEDULE("Horario de clase", Icons.Rounded.Schedule, true),
    GRADES("Calificaciones", Icons.Rounded.FactCheck, true),
    PROFILE("Perfil", Icons.Rounded.Person, true),
    ETS("ETS", Icons.Rounded.FactCheck, true),
    KARDEX("Kárdex", Icons.Rounded.HistoryEdu, true),
    ETS_CALENDAR("Calendario de ETS", Icons.Rounded.DateRange),
    RE_REGISTRATION_APPOINTMENT("Cita de reinscripción", Icons.Rounded.EventAvailable),
    OCCUPANCY("Ocupabilidad de horario", Icons.Rounded.LockClock),
    AGENDA("Agenda", Icons.Rounded.PendingActions),
    SCHOOL_SCHEDULE("Horario general", Icons.Rounded.HistoryToggleOff),
    PERFORMANCE("Rendimiento escolar", Icons.Rounded.Insights, true)
}