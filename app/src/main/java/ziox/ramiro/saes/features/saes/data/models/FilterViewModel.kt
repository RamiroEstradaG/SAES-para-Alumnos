package ziox.ramiro.saes.features.saes.data.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.filterIsInstance
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

abstract class FilterViewModel: BaseViewModel<ViewModelState, ViewModelEvent>() {
    val fieldFilterStates = states.filterIsInstance<FilterState>()

    @Composable
    fun fieldFilterStatesAsState() = fieldFilterStates.collectAsState(initial = null)

    abstract fun getFilterFields(): Any
    abstract fun selectFilterField(itemId: String, newIndex: Int?): Any
}

sealed class FilterState: ViewModelState{
    class FilterLoading : FilterState()
    class FilterComplete(val filterFields: List<FilterField>): FilterState()
}

data class SelectFilterField(
    override val itemId: String,
    override val fieldName: String,
    val selectedIndex: Int?,
    val indexOffset: Int,
    val items: List<String>
): FilterField {
    override val isSelected: Boolean = selectedIndex != null
}


interface FilterField {
    val itemId: String
    val isSelected: Boolean
    val fieldName: String
}


interface FilterRepository{
    suspend fun getFilters(): List<FilterField>
    suspend fun selectFilterField(fieldId: String, newIndex: Int?): List<FilterField>
}