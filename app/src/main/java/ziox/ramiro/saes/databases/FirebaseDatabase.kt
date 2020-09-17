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
    val escuela : String,
    val carrera: String,
    val isMediaSuperior : Boolean,
    val calificaciones: List<Float> = listOf(),
    val promedios: List<Float> = listOf(),
    val lastPeriodo : String = "",
    val exists: Boolean = true,
    val calendariosId : List<String> = listOf()
)

data class Evento(
    val dia: Long,
    val titulo: String,
    val materia: String,
    val tipo : String,
    val info: String,
    val activado: Boolean = true,
    val parent: String,
    val id: String = ""
)

data class Calendario(
    val admin : List<String>,
    val codigo : String,
    val name : String,
    val private: Boolean = true
)

fun enablePersistance(boolean: Boolean){
    db.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(boolean)
        .build()
}

fun updateToken(context: Context?, token: String) = db.collection("users").document(getHashUserId(context)).update(mapOf(
    "messagingToken" to token
))

fun addUser(boleta: String, user: User) = db.collection("users").document(HashUtils.sha256(boleta + user.escuela)).set(mapOf(
    "boleta" to boleta,
    "escuela" to user.escuela,
    "carrera" to user.carrera,
    "isMediaSuperior" to user.isMediaSuperior,
    "calificaciones" to user.calificaciones,
    "promedios" to user.promedios,
    "lastPeriodo" to user.lastPeriodo,
    "calendariosId" to user.calendariosId
))

fun updateUser(boleta: String, user: User) : Task<Void>{
    val map = hashMapOf<String, Any>(
        "boleta" to boleta,
        "escuela" to user.escuela,
        "carrera" to user.carrera,
        "isMediaSuperior" to user.isMediaSuperior
    )

    if (user.calendariosId.isNotEmpty()){
        map["calendariosId"] = user.calendariosId
    }

    if (user.calendariosId.isNotEmpty()){
        map["calificaciones"] = user.calificaciones
    }

    if (user.calendariosId.isNotEmpty()){
        map["promedios"] = user.promedios
    }

    if(user.lastPeriodo.isNotBlank()){
        map["lastPeriodo"] = user.lastPeriodo
    }


    return db.collection("users").document(HashUtils.sha256(boleta + user.escuela)).update(map)
}

fun initUser(context: Context?, boleta: String, user: User, onComplete: () -> Unit = {}) {
    if(context?.isNetworkAvailable() == true){
        getUserData(context){
            if(it.exists){
                updateUser(boleta, user).addOnSuccessListener {
                    onComplete()
                }
            }else{
                addUser(boleta, user).addOnSuccessListener {
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

fun getCalendarios(id: List<String>, onComplete: (calendarios: List<Calendario>) -> Unit){
    if(id.isEmpty()){
        onComplete(listOf())
        return
    }

    db.collection("calendarios").whereIn("codigo", id).get().addOnSuccessListener {
        val arr = ArrayList<Calendario>()
        for(doc in it.documents){
            arr.add(
                Calendario(
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

fun removeCalendario(context: Context?, codigo: String) = db.collection("users").document(
    getHashUserId(context)
).update(
    "calendariosId", FieldValue.arrayRemove(codigo)
)

fun addCalendario(context: Context? ,name: String, id : String, private: Boolean) : Task<Void>{
    return db.collection("calendarios").document(id).set(hashMapOf(
        "admin" to listOf(getHashUserId(context)),
        "codigo" to id,
        "nombre" to name,
        "private" to private
    ))
}

fun addCalendarioToUser(context: Context?, id : String) = db.collection("users").document(
    getHashUserId(context)
).update(
    "calendariosId", FieldValue.arrayUnion(id)
)

fun getEventos(codigo: String, onChange: (evento: List<Evento>) -> Unit = {}){
    db.collection("calendarios").document(codigo).collection("eventos").addSnapshotListener { documentSnapshot, _ ->
        onChange(documentSnapshot?.documents?.map {
            Evento(
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

fun addEvento(codigo: String, evento: Evento) = db.collection("calendarios").document(codigo).collection("eventos").add(mapOf(
    "dia" to evento.dia,
    "titulo" to evento.titulo,
    "materia" to evento.materia,
    "tipo" to evento.tipo,
    "info" to evento.info,
    "activado" to evento.activado,
    "parent" to codigo
))

fun updateEvento(codigo : String, evento : Evento) = db.collection("calendarios").document(codigo)
                                                        .collection("eventos").document(evento.id).set(mapOf(
    "dia" to evento.dia,
    "titulo" to evento.titulo,
    "materia" to evento.materia,
    "tipo" to evento.tipo,
    "info" to evento.info,
    "activado" to evento.activado,
    "parent" to codigo
))

fun removeEvento(codigo: String, id: String) = db.collection("calendarios").document(codigo).collection("eventos").document(id).delete()

fun getAdminCalendar(context: Context?, onComplete: (calendarios: List<Calendario>) -> Unit){
    db.collection("calendarios").whereArrayContains("admin", getHashUserId(context)).get().addOnSuccessListener {
        val arr = ArrayList<Calendario>()
        for(doc in it.documents){
            arr.add(
                Calendario(
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

fun getAllEventosSiguientes(context: Context?, onComplete: (eventos: List<Evento>) -> Unit){
    val now = Calendar.getInstance()
    now.add(Calendar.DATE, 1)
    now.set(Calendar.HOUR_OF_DAY, 0)
    now.set(Calendar.MINUTE, 0)
    now.set(Calendar.SECOND, 0)

    initUser(context, getBoleta(context), getBasicUser(context)){
        getUserData(context){
            if(it.calendariosId.isEmpty()) return@getUserData

            db.collectionGroup("eventos").whereIn("parent", it.calendariosId)
                .whereGreaterThan("dia", now.timeInMillis)
                .get().addOnSuccessListener { snap ->
                onComplete(snap.documents.mapNotNull { doc->
                    if(doc.data == null){
                        null
                    }else{
                        Evento(
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