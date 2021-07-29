package ziox.ramiro.saes.utils

import androidx.compose.runtime.*

class MutableStateWithValidation<T>(
    val mutableState: MutableState<T>,
    val errorState: MutableState<String?>,
    private val validator: (T) -> String?
){


    fun validate(): Boolean{
        val validationResult = validator(mutableState.value)
        errorState.value = validationResult

        return validationResult == null
    }
}

fun List<MutableStateWithValidation<*>>.validate() : Boolean {
    var result = true

    for (state in this){
        val isNotValid = !state.validate()
        if(result && isNotValid) {
            result = false
        }
    }

    return result
}