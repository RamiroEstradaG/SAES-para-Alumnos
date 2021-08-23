package ziox.ramiro.saes.data.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Manejador de estados de validacion y error para el [SAESTextField]
 * @param initialValue Valor inicial del TextField
 * @param stringAdapter Convierte el Generico T a String por medio de un adaptador proporcionado por el desarrollador
 * @param validator Se encarga de validar el TextField al llamar a [validate]. En caso de que el validador retorne un String significa que ocurrio un error de validacion. En caso de que retorne null significa que no hay errores de validacion.
 * @author @RamiroEda
 */
class ValidatorState<T>(
    initialValue: T,
    private val stringAdapter: (T) -> String = {it.toString()},
    private val validateBeforeChange: Boolean = false,
    private val validator: (T) -> String? = {null}
) {
    /**
     * Acceso al valor guardado, este valor no se debe usar como estado de un [Composable] ya que no refrescará la vista.
     * En caso de querer obtener el estado para actualizar la interfaz usar la funcion [state].
     */
    var value: T = initialValue
        set(value) {
            if(validateBeforeChange && validate(value)){
                lastValidValue = value
            }
            this.valueFlow.value = value
            stringValue.value = stringAdapter(value)
            field = value
        }

    var lastValidValue: T = initialValue
        private set

    /**
     * Flujo de eventos para el valor guardado
     */
    private val valueFlow = MutableStateFlow(initialValue)

    /**
     * Flujo de eventos para el valor guardado en String
     */
    private val stringValue = MutableStateFlow(stringAdapter(initialValue))

    /**
     * Flujo de eventos de errores de validacion por parte del Front-end
     */
    val error = errorStateFlow()

    /**
     * Estados del valor guardado, permite retornar un valor que refresca la interfaz
     */
    @Composable
    fun state() = valueFlow.collectAsState()

    /**
     * Estados del valor guardado en String, permite retornar un valor que refresca la interfaz
     */
    @Composable
    fun stringState() = stringValue.collectAsState()

    /**
     * Valida el valor guardado con el validador proporcionado
     * @return Si el valor guardado es valido o no
     */
    fun validate(): Boolean {
        val errorMessage = validator(valueFlow.value)

        error.value = errorMessage

        return errorMessage == null
    }

    private fun validate(value: T): Boolean{
        val errorMessage = validator(value)

        error.value = errorMessage

        return errorMessage == null
    }
}

/**
 * Utilidad para el inicio rapido de un estado para errores
 * @author @RamiroEda
 */
fun errorStateFlow() = MutableStateFlow<String?>(null)


/**
 * Validador de un conjunto de [ValidatorState]
 * @return Si todas las validaciones son [true]
 */
fun Array<ValidatorState<*>>.validate(): Boolean {
    var isValid = true

    this.forEach {
        if(!it.validate()) isValid = false
    }

    return isValid
}