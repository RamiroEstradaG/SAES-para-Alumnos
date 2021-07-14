package ziox.ramiro.saes.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T>runOnDefaultThread(block: () -> T) : T = withContext(Dispatchers.Default){
    block()
}


suspend fun <T>runOnMainThread(block: () -> T) : T = withContext(Dispatchers.Main){
    block()
}