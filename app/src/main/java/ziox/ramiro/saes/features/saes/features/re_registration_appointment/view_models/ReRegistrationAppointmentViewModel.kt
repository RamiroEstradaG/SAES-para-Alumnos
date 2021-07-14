package ziox.ramiro.saes.features.saes.features.re_registration_appointment.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationRepository

class ReRegistrationAppointmentViewModel(
    private val reRegistrationRepository: ReRegistrationRepository
) : BaseViewModel<ReRegistrationAppointmentState, ReRegistrationAppointmentEvent>() {
    init {
        fetchReRegistrationData()
    }

    private fun fetchReRegistrationData() = viewModelScope.launch {
        emitState(ReRegistrationAppointmentState.Loading())

        kotlin.runCatching {
            reRegistrationRepository.getReRegistrationData()
        }.onSuccess {
            emitState(ReRegistrationAppointmentState.Complete(it))
        }.onFailure {
            emitEvent(ReRegistrationAppointmentEvent.Error("Error al obtener los datos de la cita"))
        }
    }
}