package ziox.ramiro.saes.features.saes.features.re_registration_appointment.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.models.ReRegistrationData

sealed class ReRegistrationAppointmentState : ViewModelState{
    class Loading: ReRegistrationAppointmentState()
    class Complete(val data: ReRegistrationData): ReRegistrationAppointmentState()
}


sealed class ReRegistrationAppointmentEvent : ViewModelEvent{
    class Error(val message: String): ReRegistrationAppointmentEvent()
}
