package ziox.ramiro.saes.data.models

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

open class BaseViewModel<S: ViewModelState, E: ViewModelEvent> : ViewModel() {
    private val _states = MutableStateFlow<S?>(null)
    private val _events = MutableStateFlow<E?>(null)

    val states = _states.asSharedFlow()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _states.collect {
                if (it != null) {
                    Log.d("ViewModelState", it.javaClass.simpleName.toString())
                }
            }
        }
        viewModelScope.launch {
            _events.collect {
                if (it != null) {
                    Log.d("ViewModelEvent", it.javaClass.simpleName.toString())
                }
            }
        }
    }

    @Composable
    fun statesAsState() = _states.collectAsState()

    @Composable
    fun eventsAsState() = _events.collectAsState()

    @Composable
    fun filterStates(vararg clazz: KClass<*>) = _states.filter { state ->
        clazz.any { it.isInstance(state) }
    }.collectAsState(initial = null)

    @Composable
    fun filterEvents(vararg clazz: KClass<*>) = _events.filter { event ->
        clazz.any { it.isInstance(event) }
    }.collectAsState(initial = null)

    protected fun emitState(value: S?) = viewModelScope.launch {
        _states.emit(value)

    }

    protected fun emitEvent(value: E?) = viewModelScope.launch {
        _events.emit(value)
    }
}

interface ViewModelState
interface ViewModelEvent