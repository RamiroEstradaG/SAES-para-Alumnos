package ziox.ramiro.saes.features.saes.view_models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.data.repositories.HistoryRoomRepository
import ziox.ramiro.saes.utils.runOnDefaultThread

class SAESViewModel(
    private val historyRoomRepository: HistoryRoomRepository
) : BaseViewModel<SAESState, SAESEvent>() {
    companion object {
        val SECTION_INITIAL = MenuSection.AGENDA
    }

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
        history.removeLast()
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
    KARDEX("Kárdex", Icons.Rounded.ListAlt, true),
    ETS_CALENDAR("Calendario de ETS", Icons.Rounded.DateRange),
    RE_REGISTRATION_APPOINTMENT("Cita de reinscripción", Icons.Rounded.EventAvailable),
    OCCUPANCY("Ocupabilidad de horario", Icons.Rounded.LockClock),
    AGENDA("Agenda", Icons.Rounded.PendingActions)
}