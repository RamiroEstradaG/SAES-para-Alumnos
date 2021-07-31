package ziox.ramiro.saes.features.settings.view_models

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ziox.ramiro.saes.BuildConfig
import ziox.ramiro.saes.data.models.NotificationBuilder
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaFirebaseRepository
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnIOThread
import java.io.File
import java.util.*

class PersonalSavedDataViewModel(
    context: Context
) : ViewModel(){
    private val downloadsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    private val userFirebaseRepository = UserFirebaseRepository()
    private val agendaFirebaseRepository = AgendaFirebaseRepository()
    private val userPreferences = UserPreferences.invoke(context)
    private val notification = NotificationBuilder(context)
    val isDownloading = mutableStateOf(false)
    val isDeleting = mutableStateOf(false)
    val error = MutableStateFlow<String?>(null)
    val info = MutableStateFlow<String?>(null)

    companion object{
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "personal_data"
        const val CHANNEL_NAME = "Datos personales"
        const val CHANNEL_DESCRIPTION = "Notificaciones para la eliminacion o descarga de datos personales"
    }

    init {
        error.dismissAfterTimeout()
        info.dismissAfterTimeout()
    }

    fun downloadMyPersonalData() = viewModelScope.launch {
        isDownloading.value = true
        kotlin.runCatching {
            userFirebaseRepository.isUserRegistered(userPreferences.getPreference(PreferenceKeys.Boleta, ""))
        }.onSuccess {
            if(it){
                kotlin.runCatching {
                    val file = File("$downloadsDirectory/backup_${Date().time}.txt").also { file ->
                        if(!file.exists()){
                            file.createNewFile()
                        }
                    }
                    info.value = "Iniciando descarga. Espera un momento..."

                    val userData = userFirebaseRepository.getUserData()
                    val calendarData = agendaFirebaseRepository.getCalendars().first()
                    val eventsData = calendarData.map { calendar ->
                        agendaFirebaseRepository.getEvents(calendar.calendarId).first()
                    }

                    val fileText = """
=================== UserData ===================
$userData

=================== Agendas ===================
${calendarData.joinToString("\n")}

=================== Events ===================
${eventsData.joinToString("\n")}
                    """.trimIndent()

                    println(fileText)

                    runOnIOThread {
                        file.writeText(fileText)
                    }
                    println(file.absolutePath)
                    file
                }.onSuccess { file ->
                    notification
                        .setChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_DESCRIPTION)
                        .setTitle("Datos almacenados en la nube")
                        .setDescription("Se han descargado tus datos personales en la carpeta de descargas")
                        .setPendingIntent(Intent(Intent.ACTION_VIEW).apply {
                            val uri = FileProvider.getUriForFile(notification.context, "${BuildConfig.APPLICATION_ID}.provider", file)
                            val mime = notification.context.contentResolver.getType(uri)
                            setDataAndType(uri, mime)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        })
                        .buildAndNotify(NOTIFICATION_ID)
                }.onFailure { throwable ->
                    throwable.printStackTrace()
                    error.value = "Error al descargar los datos personales"
                }
            }else{
                info.value = "No se ha encontrado al usuario en el sistema"
            }
        }.onFailure {
            error.value = "Error al obtener el usuario"
        }
        isDownloading.value = false
    }

    fun deleteMyPersonalData() = viewModelScope.launch {
        isDeleting.value = true
        kotlin.runCatching {
            userFirebaseRepository.isUserRegistered(userPreferences.getPreference(PreferenceKeys.Boleta, ""))
        }.onSuccess {
            if(it){
                kotlin.runCatching {
                    userFirebaseRepository.deleteUser()
                }.onSuccess { isDeleted ->
                    if(isDeleted){
                        userPreferences.removePreference(PreferenceKeys.IsFirebaseEnabled)
                        userFirebaseRepository.signOut()
                        info.value = "Usuario eliminado"
                    }else{
                        info.value = "Usuario no eliminado"
                    }
                }.onFailure {
                    error.value = "Error al eliminar el usuario"
                }
            }else{
                info.value = "No se ha encontrado al usuario en el sistema"
            }
        }.onFailure {
            error.value = "Error al obtener el usuario"
        }

        isDeleting.value = false
    }
}