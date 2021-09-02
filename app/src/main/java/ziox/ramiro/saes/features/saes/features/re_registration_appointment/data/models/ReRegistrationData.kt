package ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.models

import java.util.*

data class ReRegistrationData(
    val userId: String,
    val appointmentDate: Date?,
    val appointmentDateExpiration: Date?,
    val creditsTotal: Double,
    val creditsMaximum: Double,
    val creditsMedium: Double,
    val creditsMinimum: Double,
    val creditsObtained: Double,
    val careerMediumDuration: Int,
    val careerMaximumDuration: Int,
    val careerCurrentDuration: Int
)
