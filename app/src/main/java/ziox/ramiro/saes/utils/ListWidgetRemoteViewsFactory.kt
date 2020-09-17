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
import ziox.ramiro.saes.databases.CorreccionHorarioDatabase
import ziox.ramiro.saes.databases.HorarioDatabase
import java.util.*


/**
 * Creado por Ramiro el 15/04/2019 a las 03:33 PM para SAESv2.
 */
class ListWidgetRemoteViewsFactory (val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    val data = ArrayList<ClaseData>()

    override fun onCreate() {
        getData()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long {
        return try{
            data[position].id.hashCode().toLong()
        }catch (e : Exception){
            Log.e("AppException", e.toString())
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
        rv.setTextViewText(R.id.claseNombre, data[position].materia.toProperCase())
        rv.setTextViewText(R.id.widgetHoraInicio, data[position].horaInicio.toHour())
        rv.setTextViewText(R.id.widgetHoraFinal, data[position].horaFinal.toHour())
        rv.setTextViewText(R.id.claseProfe, data[position].profesor.toProperCase())
        rv.setTextViewText(R.id.claseEdificio, data[position].edificio)
        rv.setTextViewText(R.id.claseSalon, data[position].salon)

        return rv
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun onDestroy() {

    }

    override fun getViewTypeCount(): Int = 1

    private fun getData(){
        val correccionHorarioDatabase = CorreccionHorarioDatabase(context)
        correccionHorarioDatabase.createTable()

        val horarioDatabase = HorarioDatabase(context)
        horarioDatabase.createTable()
        val all = horarioDatabase.getAll()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
        this.data.clear()

        while (all.moveToNext()){
            val dataHorario = HorarioDatabase.cursorAsClaseData(all)
            val row = correccionHorarioDatabase.searchData(dataHorario) ?: dataHorario

            if(row.diaIndex == day){
                this.data.add(row)
            }
        }

        this.data.sortBy {
            it.horaInicio
        }

        all.close()
    }
}