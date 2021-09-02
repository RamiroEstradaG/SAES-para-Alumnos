package ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories

import android.content.Context
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.models.ReRegistrationData
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.toDate

interface ReRegistrationRepository {
    suspend fun getReRegistrationData(): ReRegistrationData
}

class ReRegistrationWebViewRepository(
    private val context: Context
) : ReRegistrationRepository{
    private val webViewProvider = WebViewProvider(context, "/Alumnos/Reinscripciones/fichas_reinscripcion.aspx")

    override suspend fun getReRegistrationData(): ReRegistrationData {
        return webViewProvider.scrap(
            script = """
                var appointmentTable = byId("ctl00_mainCopy_grvEstatus_alumno");
                
                var appointmentDate = "";
                var appointmentDateExpiration = "";
                
                if(appointmentTable != null){
                    var appointmentData = appointmentTable.getElementsByTagName("tr")[1].children;
                    
                    appointmentDate = appointmentData[2].innerText.trim();
                    appointmentDateExpiration = appointmentData[3].innerText.trim();
                }
                
                var careerTable = byId("ctl00_mainCopy_CREDITOSCARRERA");
                var careerData = careerTable.getElementsByTagName("tr")[1].children;
                
                var trajectoryTable = byId("ctl00_mainCopy_alumno");
                var trajectoryData = trajectoryTable.getElementsByTagName("tr")[1].children;
                
                next({
                    appointmentDate: appointmentDate,
                    appointmentDateExpiration: appointmentDateExpiration,
                    creditsTotal: careerData[1].innerText.trim(),
                    creditsMaximum: careerData[2].innerText.trim(),
                    creditsMedium: careerData[3].innerText.trim(),
                    creditsMinimum: careerData[4].innerText.trim(),
                    creditsObtained: trajectoryData[1].innerText.trim(),
                    careerMediumDuration: careerData[5].innerText.trim(),
                    careerMaximumDuration: careerData[6].innerText.trim(),
                    careerCurrentDuration: trajectoryData[3].innerText.trim()
                });
            """.trimIndent()
        ){
            val data = it.result.getJSONObject("data")

            ReRegistrationData(
                UserPreferences.invoke(context).getPreference(PreferenceKeys.Boleta, ""),
                data.getString("appointmentDate").safeReplaceHour().toDate("dd/MM/yyyy hh:mm:ss a"),
                data.getString("appointmentDateExpiration").safeReplaceHour().toDate("dd/MM/yyyy hh:mm:ss a"),
                data.getString("creditsTotal").toDouble(),
                data.getString("creditsMaximum").toDouble(),
                data.getString("creditsMedium").toDouble(),
                data.getString("creditsMinimum").toDouble(),
                data.getString("creditsObtained").toDouble(),
                data.getString("careerMediumDuration").toInt(),
                data.getString("careerMaximumDuration").toInt(),
                data.getString("careerCurrentDuration").toInt()
            )
        }
    }

    private fun String.safeReplaceHour() = this.replace("a. m.", "AM").replace("p. m.", "PM")
}