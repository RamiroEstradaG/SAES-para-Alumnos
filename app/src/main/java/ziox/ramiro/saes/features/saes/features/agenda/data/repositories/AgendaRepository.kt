package ziox.ramiro.saes.features.saes.features.agenda.data.repositories

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaEventType
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.utils.MMMddyyyy_toDate
import ziox.ramiro.saes.utils.offset
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

interface AgendaRepository {
    suspend fun getEvents(calendarId: String): Flow<List<AgendaItem>>
    suspend fun addEvent(agendaItem: AgendaItem)
    suspend fun removeEvent(calendarId: String, eventId:String)
    suspend fun editEvent(agendaItem: AgendaItem): AgendaItem
    suspend fun getCalendars(): Flow<List<AgendaCalendar>>
    suspend fun addCalendar(name: String)
    suspend fun removeCalendar(calendarId: String)
    suspend fun joinCalendar(userId: String, code: String): AgendaCalendar?
}


class AgendaWebViewRepository(
    context: Context
) : AgendaRepository{
    private val webViewProvider = WebViewProvider(context, "/Academica/agenda_escolar.aspx")
    private val firebaseRepository = AgendaFirebaseRepository()

    @OptIn(ExperimentalTime::class)
    override suspend fun getEvents(calendarId: String): Flow<List<AgendaItem>> {
        val saesEvents = try{
            webViewProvider.scrap(
                script = """
                var agendaTable = byId("ctl00_mainCopy_GVAgenda");
                
                if(agendaTable != null){
                    var trs = [...agendaTable.getElementsByTagName("tr")];
                    
                    trs.splice(0,1);
                    
                    next(trs.map((value) => ({
                        eventName: value.children[0].innerText.trim(),
                        start: value.children[1].innerText.trim(),
                        end: value.children[2].innerText.trim()
                    })));
                }else{
                    next([]);
                }
            """.trimIndent()
            ){
                val data = it.result.getJSONArray("data")

                val events = ArrayList<AgendaItem>()

                for (i in 0 until data.length()){
                    val item = data[i] as JSONObject

                    val start = item.getString("start").MMMddyyyy_toDate()
                    var offset = 0
                    val end = ShortDate.MMMddyyyy(item.getString("end"))


                    do {
                        val dateWithOffset = start.offset(Duration.days(offset++))
                        val currentDate = ShortDate.fromDate(dateWithOffset)

                        events.add(AgendaItem(
                            eventName = item.getString("eventName"),
                            date = currentDate,
                            calendarId = "SAES",
                            scheduleDayTime = ScheduleDayTime(Hour(12,0), Hour(13,0), WeekDay.byDate(dateWithOffset)),
                            eventType = AgendaEventType.ACADEMIC
                        ))
                    } while (currentDate != end)
                }

                events
            }
        }catch (e: Exception){
            arrayListOf()
        }

        return firebaseRepository.getEvents(calendarId).map {
            it.addAll(saesEvents)
            it
        }
    }

    override suspend fun addEvent(agendaItem: AgendaItem) = firebaseRepository.addEvent(agendaItem)
    override suspend fun removeEvent(calendarId: String, eventId: String) = firebaseRepository.removeEvent(calendarId, eventId)
    override suspend fun editEvent(agendaItem: AgendaItem) = firebaseRepository.editEvent(agendaItem)
    override suspend fun getCalendars() = firebaseRepository.getCalendars()
    override suspend fun addCalendar(name: String) = firebaseRepository.addCalendar(name)
    override suspend fun removeCalendar(calendarId: String) = firebaseRepository.removeCalendar(calendarId)
    override suspend fun joinCalendar(userId: String, code: String) = firebaseRepository.joinCalendar(userId, code)
}


class AgendaFirebaseRepository : AgendaRepository{
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val userRepository = UserFirebaseRepository()

    companion object {
        const val COLLECTION_ID_CALENDARS = "calendars_v2"
        const val COLLECTION_ID_EVENTS = "events_v2"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getEvents(calendarId: String) = callbackFlow {
        val subs = db
            .collection(COLLECTION_ID_CALENDARS)
            .document(calendarId)
            .collection(COLLECTION_ID_EVENTS)
            .addSnapshotListener { value, error ->
                error?.printStackTrace()
                if(value != null){
                    trySend(value.toObjects(AgendaItem::class.java))
                }
            }

        awaitClose { subs.remove() }
    }

    override suspend fun addEvent(agendaItem: AgendaItem) {
        db.collection(COLLECTION_ID_CALENDARS)
            .document(agendaItem.calendarId)
            .collection(COLLECTION_ID_EVENTS)
            .add(agendaItem)
            .await()
    }

    override suspend fun removeEvent(calendarId: String, eventId: String) {
        db.collection(COLLECTION_ID_CALENDARS)
            .document(calendarId)
            .collection(COLLECTION_ID_EVENTS)
            .document(eventId)
            .delete()
            .await()
    }

    override suspend fun editEvent(agendaItem: AgendaItem): AgendaItem {
        val ref = db.collection(COLLECTION_ID_CALENDARS)
            .document(agendaItem.calendarId)
            .collection(COLLECTION_ID_EVENTS)
            .document(agendaItem.eventId)

        ref.update(agendaItem.toJson())
            .await()

        return ref.get().await().toObject(AgendaItem::class.java)!!
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCalendars() = userRepository.getUserDataFlow().map {
        if(it.calendarIds.isNotEmpty()){
            db.collection(COLLECTION_ID_CALENDARS)
                .whereIn("calendarId", it.calendarIds)
                .get()
                .await()
                .toObjects(AgendaCalendar::class.java)
        }else{
            listOf()
        }
    }

    override suspend fun addCalendar(name: String) {
        val currentUser = auth.currentUser

        val calendar = db.collection(COLLECTION_ID_CALENDARS)
            .add(AgendaCalendar(
                name = name,
                admins = listOf(currentUser?.uid ?: "")
            )).await()

        userRepository.updateUserField("calendarIds", FieldValue.arrayUnion(calendar.get().await().id))
    }

    override suspend fun removeCalendar(calendarId: String) {
        db.collection(COLLECTION_ID_CALENDARS)
            .document(calendarId)
            .delete().await()
    }

    override suspend fun joinCalendar(userId: String, code: String): AgendaCalendar? {
        val calendars = db.collection(COLLECTION_ID_CALENDARS)
            .whereEqualTo("code", code)
            .get()
            .await()
            .toObjects(AgendaCalendar::class.java)

        val calendar = calendars.singleOrNull()

        if(calendar != null){
            userRepository.updateUserField(COLLECTION_ID_CALENDARS, FieldValue.arrayUnion(calendar.calendarId))
        }

        return calendar
    }
}