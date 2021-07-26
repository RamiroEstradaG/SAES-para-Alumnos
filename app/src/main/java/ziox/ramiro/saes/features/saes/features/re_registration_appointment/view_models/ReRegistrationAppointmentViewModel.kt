package ziox.ramiro.saes.features.saes.features.re_registration_appointment.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.models.ReRegistrationData
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class ReRegistrationAppointmentViewModel(
    private val reRegistrationRepository: ReRegistrationRepository
) : ViewModel() {
    val reRegistrationData = mutableStateOf<ReRegistrationData?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchReRegistrationData()
        error.dismissAfterTimeout()
    }

    private fun fetchReRegistrationData() = viewModelScope.launch {
        reRegistrationData.value = null

        kotlin.runCatching {
            reRegistrationRepository.getReRegistrationData()
        }.onSuccess {
            reRegistrationData.value = it
        }.onFailure {
            error.value = "Error al obtener los datos de la cita"
        }
    }
}