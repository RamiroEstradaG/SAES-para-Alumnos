package ziox.ramiro.saes.data.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel<S: ViewModelState, E: ViewModelEvent> : ViewModel() {
    private val _states = MutableStateFlow<S?>(null)
    private val _events = MutableStateFlow<E?>(null)

    val states = _states.asSharedFlow()
    val events = _events.asSharedFlow()

    protected fun emitState(value: S?) = viewModelScope.launch {
        Log.d("ViewModelState", value?.javaClass?.simpleName.toString())
        _states.emit(value)
    }

    protected fun emitEvent(value: E?) = viewModelScope.launch {
        Log.d("ViewModelEvent", value?.javaClass?.simpleName.toString())
        _events.emit(value)
    }
}

interface ViewModelState
interface ViewModelEvent