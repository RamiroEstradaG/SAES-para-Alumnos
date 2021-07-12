package ziox.ramiro.saes.features.saes.data.models

import kotlinx.coroutines.flow.filterIsInstance
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

abstract class FilterViewModel: BaseViewModel<ViewModelState, ViewModelEvent>() {
    val filterState = states.filterIsInstance<FilterState>()

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