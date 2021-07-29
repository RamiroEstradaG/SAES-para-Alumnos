package ziox.ramiro.saes.data.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fabrica de [ViewModel] usada para escribir menos codigo cuando se necesiten constructores personalizados en los [ViewModel]
 * @param function Lambda que requiere un ViewModel como retorno
 * @return La Fabrica utilizada para construir [ViewModel]
 */
@Suppress("UNCHECKED_CAST")
inline fun <VM : ViewModel> viewModelFactory(crossinline function: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = function() as T
    }