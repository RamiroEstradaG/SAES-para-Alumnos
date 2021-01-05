package ziox.ramiro.saes.databases

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import ziox.ramiro.saes.utils.*
import java.util.*
import kotlin.collections.ArrayList

private val db = FirebaseFirestore.getInstance()

data class User(
    val schoolName : String,
    val careerName: String,
    val isHighSchool : Boolean,
    val grades: List<Float> = listOf(),
    val finalScores: List<Float> = listOf(),
    val lastPeriod : String = "",
    val exists: Boolean = true,
    val calendarIds : List<String> = listOf()
)

data class CalendarEvent(
    val date: Long,
    val title: String,
    val courseName: String,
    val type : String,
    val info: String,
    val isEnable: Boolean = true,
    val parent: String,
    val id: String = ""
)

data class UserCalendar(
    val admin : List<String>,
    val code : String,
    val name : String,
    val private: Boolean = true
)

fun enablePersistence(boolean: Boolean){
    db.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(boolean)
        .build()
}

fun updateToken(context: Context?, token: String) = db.collection("users").document(getHashUserId(context)).update(mapOf(
    "messagingToken" to token
))

fun addUser(boleta: String, user: User) = db.collection("users").document(HashUtils.sha256(boleta + user.schoolName)).set(mapOf(
    "boleta" to boleta,
    "escuela" to user.schoolName,
    "carrera" to user.careerName,
    "isMediaSuperior" to user.isHighSchool,
    "calificaciones" to user.grades,
    "promedios" to user.finalScores,
    "lastPeriodo" to user.lastPeriod,
    "calendariosId" to user.calendarIds
))

fun updateUser(studentID: String, user: User) : Task<Void>{
    val map = hashMapOf<String, Any>(
        "boleta" to studentID,
        "escuela" to user.schoolName,
        "carrera" to user.careerName,
        "isMediaSuperior" to user.isHighSchool
    )

    if (user.calendarIds.isNotEmpty()){
        map["calendariosId"] = user.calendarIds
    }

    if (user.calendarIds.isNotEmpty()){
        map["calificaciones"] = user.grades
    }

    if (user.calendarIds.isNotEmpty()){
        map["promedios"] = user.finalScores
    }

    if(user.lastPeriod.isNotBlank()){
        map["lastPeriodo"] = user.lastPeriod
    }


    return db.collection("users").document(HashUtils.sha256(studentID + user.schoolName)).update(map)
}

fun initUser(context: Context?, studentID: String, user: User, onComplete: () -> Unit = {}) {
    if(context?.isNetworkAvailable() == true){
        getUserData(context){
            if(it.exists){
                updateUser(studentID, user).addOnSuccessListener {
                    onComplete()
                }
            }else{
                addUser(studentID, user).addOnSuccessListener {
                    onComplete()
                }
            }
        }
    }else{
        onComplete()
    }
}

fun getStatistics(name : String = "IPN") = db.collection("statistics").document(name).get()

@Suppress("UNCHECKED_CAST")
fun getUserData(context: Context?, onComplete: (user: User)->Unit = {}){
    db.collection("users").document(getHashUserId(context)).get().addOnSuccessListener {
        if(it.data != null){
            onComplete(
                User(
                it.data!!["escuela"] as String,
                it.data!!["carrera"] as String,
                it.data!!["isMediaSuperior"] as Boolean,
                it.data!!["calificaciones"] as List<Float>,
                it.data!!["promedios"] as List<Float>,
                it.data!!["lastPeriodo"] as String,
                it.exists(),
                it.data!!["calendariosId"] as List<String>? ?: listOf()
            )
            )
        }else{
            onComplete(
                User(
                "",
                "",
                false,
                exists = false
            )
            )
        }
    }
}

fun getUserCalendars(id: List<String>, onComplete: (calendars: List<UserCalendar>) -> Unit){
    if(id.isEmpty()){
        onComplete(listOf())
        return
    }

    db.collection("calendarios").whereIn("codigo", id).get().addOnSuccessListener {
        val arr = ArrayList<UserCalendar>()
        for(doc in it.documents){
            @Suppress("UNCHECKED_CAST")
            arr.add(
                UserCalendar(
                doc.data!!["admin"] as? List<String> ?: listOf(),
                doc.data!!["codigo"] as String,
                doc.data!!["nombre"] as String,
                doc.data!!["private"] as Boolean
            )
            )
        }

        onComplete(arr)
    }
}

fun removeCalendar(context: Context?, codigo: String) = db.collection("users").document(
    getHashUserId(context)
).update(
    "calendariosId", FieldValue.arrayRemove(codigo)
)

fun addCalendar(context: Context?, name: String, id : String, private: Boolean) : Task<Void>{
    return db.collection("calendarios").document(id).set(hashMapOf(
        "admin" to listOf(getHashUserId(context)),
        "codigo" to id,
        "nombre" to name,
        "private" to private
    ))
}

fun addCalendarToUser(context: Context?, id : String) = db.collection("users").document(
    getHashUserId(context)
).update(
    "calendariosId", FieldValue.arrayUnion(id)
)

fun getEvents(code: String, onChange: (event: List<CalendarEvent>) -> Unit = {}){
    db.collection("calendarios").document(code).collection("eventos").addSnapshotListener { documentSnapshot, _ ->
        onChange(documentSnapshot?.documents?.map {
            CalendarEvent(
                it.data!!["dia"] as Long,
                it.data!!["titulo"] as String? ?: "Evento",
                it.data!!["materia"] as String? ?: "",
                it.data!!["tipo"] as String? ?: "",
                it.data!!["info"] as String? ?: "Sin información",
                it.data!!["activado"] as Boolean? ?: true,
                it.data!!["parent"] as String,
                it.id
            )
        } ?: listOf())
    }
}

fun addEvent(code: String, event: CalendarEvent) = db.collection("calendarios").document(code).collection("eventos").add(mapOf(
    "dia" to event.date,
    "titulo" to event.title,
    "materia" to event.courseName,
    "tipo" to event.type,
    "info" to event.info,
    "activado" to event.isEnable,
    "parent" to code
))

fun updateEvent(code : String, event : CalendarEvent) = db.collection("calendarios").document(code)
                                                        .collection("eventos").document(event.id).set(mapOf(
    "dia" to event.date,
    "titulo" to event.title,
    "materia" to event.courseName,
    "tipo" to event.type,
    "info" to event.info,
    "activado" to event.isEnable,
    "parent" to code
))

fun removeEvent(codigo: String, id: String) = db.collection("calendarios").document(codigo).collection("eventos").document(id).delete()

fun getAdminCalendar(context: Context?, onComplete: (calendarios: List<UserCalendar>) -> Unit){
    db.collection("calendarios").whereArrayContains("admin", getHashUserId(context)).get().addOnSuccessListener {
        val arr = ArrayList<UserCalendar>()
        for(doc in it.documents){
            @Suppress("UNCHECKED_CAST")
            arr.add(
                UserCalendar(
                doc.data!!["admin"] as? List<String> ?: listOf(),
                doc.data!!["codigo"] as String,
                doc.data!!["nombre"] as String,
                doc.data!!["private"] as Boolean
            )
            )
        }

        onComplete(arr)
    }
}

fun getAllFollowingEvents(context: Context?, onComplete: (events: List<CalendarEvent>) -> Unit){
    val now = Calendar.getInstance()
    now.add(Calendar.DATE, 1)
    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)

    initUser(context, getBoleta(context), getBasicUser(context)){
        getUserData(context){
            if(it.calendarIds.isEmpty()) return@getUserData

            db.collectionGroup("eventos").whereIn("parent", it.calendarIds)
                .whereGreaterThan("dia", now.timeInMillis)
                .get().addOnSuccessListener { snap ->
                onComplete(snap.documents.mapNotNull { doc->
                    if(doc.data == null){
                        null
                    }else{
                        CalendarEvent(
                            doc.data!!["dia"] as Long,
                            doc.data!!["titulo"] as String? ?: "Evento",
                            doc.data!!["materia"] as String? ?: "",
                            doc.data!!["tipo"] as String? ?: "",
                            doc.data!!["info"] as String? ?: "Sin información",
                            doc.data!!["activado"] as Boolean? ?: true,
                            doc.data!!["parent"] as String,
                            doc.id
                        )
                    }
                })
            }.addOnFailureListener { error ->
                Log.e("FirebaseFailure", error.toString())
            }
        }
    }
}