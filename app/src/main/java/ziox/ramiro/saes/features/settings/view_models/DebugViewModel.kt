package ziox.ramiro.saes.features.settings.view_models

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.Response
import ziox.ramiro.saes.data.data_providers.ScrapException
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSRepository
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarRepository
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyRepository
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRepository
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleRepository
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val gradesRepository: GradesRepository,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val etsCalendarRepository: ETSCalendarRepository,
    private val etsRepository: ETSRepository,
    private val kardexRepository: KardexRepository,
    private val occupancyRepository: OccupancyRepository,
    private val schoolScheduleRepository: SchoolScheduleRepository,
    private val reRegistrationRepository: ReRegistrationRepository
) : ViewModel() {
    val isLoading = mutableStateOf(false)
    val downloadedData = mutableStateOf<String?>(null)
    val error = MutableStateFlow<String?>(null)

    fun downloadAllData() = viewModelScope.launch {
        isLoading.value = true
        error.value = null
        downloadedData.value = null

        val dataFunctions = listOf<suspend () -> Response<*>>(
            suspend { authRepository.getCaptcha() },
            suspend { gradesRepository.getMyGrades() },
            suspend { profileRepository.getMyUserData() },
            suspend { scheduleRepository.getMySchedule() },
            suspend { etsCalendarRepository.getETSEvents() },
            suspend { etsRepository.getAvailableETS() },
            suspend { etsRepository.getETSScores() },
            suspend { kardexRepository.getMyKardexData() },
            suspend { occupancyRepository.getOccupancyData() },
            suspend { schoolScheduleRepository.getSchoolSchedule() },
            suspend { reRegistrationRepository.getReRegistrationData() }
        )

        val results = StringBuilder()

        dataFunctions.forEachIndexed { index, function ->
            runCatching {
                val response = function()

                results.appendLine(
                    """
============================== ${response.url} ==============================
${response.sourceCode}\n\n\n\n\n
                """.trimIndent()
                )
            }.onFailure {
                if (it is ScrapException && it.sourceCode != null) {
                    results.appendLine(
                        """
============================== ${it.url ?: "unknown_${index}"} (ERROR: ${it.message}) ==============================
${it.sourceCode}\n\n\n\n\n
                    """.trimIndent()
                    )
                }
            }
        }

        downloadedData.value = results.toString()

        isLoading.value = false
    }

    fun saveFileToUri(destination: Uri) = viewModelScope.launch {
        val files = downloadedData.value

        if (files == null) {
            error.value = "No hay archivos para guardar"
            return@launch
        }

        runCatching {
            val outputStream =
                context.contentResolver.openOutputStream(destination)
                    ?: throw Exception("No se pudo abrir el URI de destino")

            outputStream.use { output ->
                output.write(files.toByteArray())
            }
        }.onFailure {
            error.value = "Error al guardar el archivo: ${it.message}"
        }
    }
}