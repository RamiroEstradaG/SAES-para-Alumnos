package ziox.ramiro.saes.features.saes.features.profile.data.repositories

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.profile.data.models.Address
import ziox.ramiro.saes.features.saes.features.profile.data.models.ContactInformation
import ziox.ramiro.saes.features.saes.features.profile.data.models.Education
import ziox.ramiro.saes.features.saes.features.profile.data.models.Parent
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfilePicture
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.ddMMMyyyy_toDate
import ziox.ramiro.saes.utils.isNetworkAvailable
import ziox.ramiro.saes.utils.runOnDefaultThread
import ziox.ramiro.saes.utils.toProperCase

interface ProfileRepository {
    suspend fun getMyUserData() : ProfileUser
}

class ProfileWebViewRepository(
    private val context: Context,
    withTestFile: String? = null
) : ProfileRepository{
    private val webViewProvider = WebViewProvider(context, "/Alumnos/info_alumnos/Datos_Alumno.aspx", withTestFile)
    private val persistenceRepository = LocalAppDatabase.invoke(context).userRepository()

    override suspend fun getMyUserData(): ProfileUser {
        return if(context.isNetworkAvailable()){
            webViewProvider.scrap(
                script = """
                try {
                    next({
                        id: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_Boleta").innerText.trim(),
                        name: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_Nombre").innerText.trim(),
                        school: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_Plantel").innerText.trim(),
                        curp: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_CURP").innerText.trim(),
                        rfc: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_RFC").innerText.trim(),
                        gender: byId("ctl00_mainCopy_TabContainer1_Tab_Generales_Lbl_Sexo").innerText.trim(),
                        birthday: byId("ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_FecNac").innerText.trim(),
                        nationality: byId("ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_Nacionalidad").innerText.trim(),
                        state: byId("ctl00_mainCopy_TabContainer1_TabPanel1_Lbl_EntNac").innerText.trim(),
                        isWorking: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Labora").innerText.trim(),
                        address: {
                            street: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Calle").innerText.trim(),
                            extNumber: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumExt").innerText.trim(),
                            intNumber: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_NumInt").innerText.trim(),
                            suburb: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Colonia").innerText.trim(),
                            zip: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_CP").innerText.trim(),
                            state: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Estado").innerText.trim(),
                            municipality: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_DelMpo").innerText.trim(),
                        },
                        contact: {
                            phone: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Tel").innerText.trim(),
                            mobilePhone: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_Movil").innerText.trim(),
                            email: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_eMail").innerText.trim(),
                            officePhone: byId("ctl00_mainCopy_TabContainer1_Tab_Direccion_Lbl_TelOficina").innerText.trim(),
                        },
                        education: {
                            highSchoolName: byId("ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_EscProc").innerText.trim(),
                            highSchoolState: byId("ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_EdoEscProc").innerText.trim(),
                            highSchoolFinalGrade: byId("ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromNMS").innerText.trim(),
                            middleSchoolFinalGrade: byId("ctl00_mainCopy_TabContainer1_Tab_Escolaridad_Lbl_PromSec").innerText.trim(),
                        },
                        parents: {
                            guardianName: byId("ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_NomTut").innerText.trim(),
                            guardianRfc: byId("ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_RFCTut").innerText.trim(),
                            father: byId("ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Padre").innerText.trim(),
                            mother: byId("ctl00_mainCopy_TabContainer1_Tab_Tutor_Lbl_Madre").innerText.trim(),
                        }
                    });
                }catch(e){
                    throwError(e);
                }
            """.trimIndent()
            ){
                val data = it.result.getJSONObject("data")
                val address = data.getJSONObject("address")
                val contact = data.getJSONObject("contact")
                val education = data.getJSONObject("education")
                val parents = data.getJSONObject("parents")

                ProfileUser(
                    data.getString("id"),
                    data.getString("name").toProperCase(),
                    data.getString("school"),
                    data.getString("curp"),
                    data.getString("rfc"),
                    data.getString("gender").lowercase() == "hombre",
                    ProfilePicture(
                        UserPreferences.invoke(context).getPreference(PreferenceKeys.SchoolUrl, "")+"/Alumnos/info_alumnos/Fotografia.aspx",
                        it.headers
                    ),
                    data.getString("birthday").ddMMMyyyy_toDate(),
                    data.getString("nationality").toProperCase(),
                    data.getString("state").toProperCase(),
                    data.getString("isWorking").uppercase() == "SI",
                    Address(
                        address.getString("street").toProperCase(),
                        address.getString("extNumber"),
                        address.getString("intNumber"),
                        address.getString("suburb").toProperCase(),
                        address.getString("zip"),
                        address.getString("state").toProperCase(),
                        address.getString("municipality").toProperCase(),
                    ),
                    ContactInformation(
                        contact.getString("phone"),
                        contact.getString("mobilePhone"),
                        contact.getString("email"),
                        contact.getString("officePhone")
                    ),
                    Education(
                        education.getString("highSchoolName").toProperCase(),
                        education.getString("highSchoolState").toProperCase(),
                        education.getString("highSchoolFinalGrade").toDouble(),
                        education.getString("middleSchoolFinalGrade").toDouble()
                    ),
                    Parent(
                        parents.getString("guardianName").toProperCase(),
                        parents.getString("guardianRfc"),
                        parents.getString("father").toProperCase(),
                        parents.getString("mother").toProperCase()
                    )
                )
            }.also {
                runOnDefaultThread {
                    persistenceRepository.removeUserData(it.id)
                    persistenceRepository.addUserData(it)
                }
            }
        }else{
            runOnDefaultThread {
                persistenceRepository.getMyUserData(UserPreferences.invoke(context).getPreference(PreferenceKeys.Boleta, ""))
            }!!
        }
    }
}

@Dao
interface ProfileRoomRepository {
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getMyUserData(id: String) : ProfileUser?

    @Insert
    fun addUserData(profileUser: ProfileUser)

    @Query("DELETE FROM profiles WHERE id = :id")
    fun removeUserData(id: String)
}