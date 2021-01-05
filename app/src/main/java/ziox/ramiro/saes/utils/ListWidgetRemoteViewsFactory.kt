package ziox.ramiro.saes.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.util.Log
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.ScheduleClass
import java.util.*


/**
 * Creado por Ramiro el 15/04/2019 a las 03:33 PM para SAESv2.
 */
class ListWidgetRemoteViewsFactory (val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    val data = ArrayList<ScheduleClass>()

    override fun onCreate() {
        getData()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long {
        return try{
            data[position].uid.hashCode().toLong()
        }catch (e : Exception){
            Log.e(this.javaClass.canonicalName, e.toString())
            data.hashCode().toLong()
        }
    }

    override fun onDataSetChanged() {
        val idToken = Binder.clearCallingIdentity()

        getData()

        Binder.restoreCallingIdentity(idToken)
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews? {
        if (position == AdapterView.INVALID_POSITION || data.isEmpty() || !data.indices.contains(position)) return null

        val rv = RemoteViews(context.packageName, R.layout.widget_view_list_item)

        rv.setInt(R.id.widgetHoraParent, "setBackgroundColor", Color.parseColor(data[position].color))
        rv.setTextViewText(R.id.course_name_text_view, data[position].courseName.toProperCase())
        rv.setTextViewText(R.id.widgetHoraInicio, data[position].startHour.toHour())
        rv.setTextViewText(R.id.widgetHoraFinal, data[position].finishHour.toHour())
        rv.setTextViewText(R.id.teacher_name_text_view, data[position].teacherName.toProperCase())
        rv.setTextViewText(R.id.building_name_text_view, data[position].buildingName)
        rv.setTextViewText(R.id.class_room_name_text_view, data[position].classroomName)

        return rv
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun onDestroy() {

    }

    override fun getViewTypeCount(): Int = 1

    private fun getData(){
        val correccionHorarioDatabase = AppLocalDatabase.getInstance(context).adjustedClassScheduleDao()

        val horarioDatabase = AppLocalDatabase.getInstance(context).originalClassScheduleDao()
        val all = horarioDatabase.getAll()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
        this.data.clear()

        for(clase in all){
            val row = correccionHorarioDatabase.get(clase.uid) ?: clase

            if(row.dayIndex == day){
                this.data.add(row)
            }
        }

        this.data.sortBy {
            it.startHour
        }
    }
}