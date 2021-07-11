package ziox.ramiro.saes.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun <T>runOnDefaultThread(block: () -> T) : T = withContext(Dispatchers.Default){
    block()
}


