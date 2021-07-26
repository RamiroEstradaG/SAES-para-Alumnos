package ziox.ramiro.saes.services

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import java.util.*


/**
 * Creado por Ramiro el 15/04/2019 a las 03:33 PM para SAESv2.
 */
class ListWidgetRemoteViewsFactory (val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val scheduleData = ArrayList<ClassSchedule>()

    override fun onCreate() {
        CoroutineScope(Dispatchers.Default).launch {
            fetchScheduleData()
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int) = scheduleData[position].id.hashCode().toLong()

    override fun onDataSetChanged() {
        CoroutineScope(Dispatchers.Default).launch {
            val idToken = Binder.clearCallingIdentity()

            fetchScheduleData()

            Binder.restoreCallingIdentity(idToken)
        }
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews? {
        if (position == AdapterView.INVALID_POSITION || scheduleData.isEmpty()) return null

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_schedule_medium_item)

        val classSchedule = scheduleData[position]

        remoteViews.setInt(R.id.widgetHoraParent, "setBackgroundColor", Color(classSchedule.color.toULong()).toArgb())
        remoteViews.setTextViewText(R.id.course_name_text_view, classSchedule.className)
        remoteViews.setTextViewText(R.id.widgetHoraInicio, classSchedule.hourRange.start.toString())
        remoteViews.setTextViewText(R.id.widgetHoraFinal, classSchedule.hourRange.end.toString())
        remoteViews.setTextViewText(R.id.teacher_name_text_view, classSchedule.teacherName)
        remoteViews.setTextViewText(R.id.building_name_text_view, classSchedule.building)
        remoteViews.setTextViewText(R.id.class_room_name_text_view, classSchedule.classroom)

        return remoteViews
    }

    override fun getCount() = scheduleData.size

    override fun onDestroy() {}

    override fun getViewTypeCount(): Int = 1

    private suspend fun fetchScheduleData() = withContext(Dispatchers.Default){
        val db = LocalAppDatabase(context).scheduleRepository()
        val scheduleList = db.getMySchedule()
        val weekDay = WeekDay.today()

        scheduleData.clear()
        scheduleData.addAll(scheduleList.filter {
            it.hourRange.weekDay == weekDay
        }.sortedBy {
            it.hourRange.start.toDouble()
        })
    }
}