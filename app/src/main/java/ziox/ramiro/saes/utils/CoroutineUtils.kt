package ziox.ramiro.saes.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

suspend fun <T>runOnDefaultThread(block: () -> T) : T = withContext(Dispatchers.Default){
    block()
}

suspend fun <T>runOnIOThread(block: () -> T) : T = withContext(Dispatchers.IO){
    block()
}

suspend fun <T>runOnMainThread(block: () -> T) : T = withContext(Dispatchers.Main){
    block()
}

fun <T>MutableStateFlow<T?>.dismissAfterTimeout(timeout: Long = 4000) = CoroutineScope(Dispatchers.Default).launch {
    collect {
        delay(timeout)
        value = null
    }
}