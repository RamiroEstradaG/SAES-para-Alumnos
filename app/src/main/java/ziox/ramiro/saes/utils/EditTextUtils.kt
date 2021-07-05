package ziox.ramiro.saes.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

data class ValidationResult(
    val isError: Boolean,
    val errorMessage: String
)

@Composable
fun validateField(
    inState: State<String>,
    validator: (String) -> String?
) : ValidationResult{
    val errorMessage = validator(inState.value)

    return if(errorMessage != null){
        ValidationResult(true, errorMessage)
    }else{
        ValidationResult(false, "")
    }
}

fun List<ValidationResult>.areAllValid() : Boolean {
    for (result in this){
        if(result.isError) return false
    }

    return true
}