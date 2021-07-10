package ziox.ramiro.saes.features.saes.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel

class SAESViewModel : BaseViewModel<SAESState, SAESEvent>() {
    companion object {
        val SECTION_INITIAL = MenuSection.KARDEX
    }

    private val _currentSection = MutableStateFlow(SECTION_INITIAL)
    val currentSection = _currentSection.asSharedFlow()

    fun changeSection(newSection: MenuSection) = viewModelScope.launch {
        _currentSection.emit(newSection)
    }
}


enum class MenuSection{
    HOME,
    SCHEDULE,
    GRADES,
    PROFILE,
    ETS,
    KARDEX
}