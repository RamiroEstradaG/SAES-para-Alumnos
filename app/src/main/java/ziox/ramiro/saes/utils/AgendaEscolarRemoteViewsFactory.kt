package ziox.ramiro.saes.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Binder
import android.view.View
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import ziox.ramiro.saes.R
import ziox.ramiro.saes.sql.AgendaEscolarDatabase
import java.util.*
import kotlin.collections.ArrayList


class AgendaEscolarRemoteViewsFactory (val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private lateinit var agendaEscolarDatabase : AgendaEscolarDatabase
    private lateinit var cursor : Cursor
    private data class EventData(val nombre: String, val fecha : GregorianCalendar?, val isFinal : Boolean?, val color : String)
    private val eventos = ArrayList<EventData>()

    override fun onCreate() {
        agendaEscolarDatabase = AgendaEscolarDatabase(context)
        agendaEscolarDatabase.createTable()

        cursor = agendaEscolarDatabase.getAll()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long {
        return eventos[position].nombre.hashCode()+when(eventos[position].isFinal){
            true -> 1L
            false -> 0L
            null -> 2L
        }
    }

    override fun onDataSetChanged() {
        if(::cursor.isInitialized){
            cursor.close()
        }

        eventos.clear()

        val idToken = Binder.clearCallingIdentity()
        val now = Calendar.getInstance()

        cursor = agendaEscolarDatabase.getAll()
        val colores = context.resources.getStringArray(R.array.paletaHorario)
        val tipoEventos = context.resources.getStringArray(R.array.tipo_eventos)

        while (cursor.moveToNext()){
            val data = AgendaEscolarDatabase.cursorAsClaseData(cursor)
            val inicio = data.inicio.split(" ")
            val final = data.final.split(" ")

            val diaInicio = GregorianCalendar(inicio[2].toInt(), MES.indexOf(inicio[0].toUpperCase(Locale.ROOT)), inicio[1].toInt(),0,0)
            val diaFinal = GregorianCalendar(final[2].toInt(), MES.indexOf(final[0].toUpperCase(Locale.ROOT)), final[1].toInt(),0,0)

            val color = if(data.laborable == 0){
                "#CCCCCC"
            }else{
                val index = tipoEventos.indexOf(data.tipoEvento)
                colores[if(index >= 0){
                    index
                }else{
                    colores.lastIndex
                }]
            }

            if (diaInicio == diaFinal) {
                if(diaInicio >= now)
                    eventos.add(EventData(data.nombre, diaInicio, null, color))
            } else {
                if(diaInicio >= now)
                    eventos.add(EventData(data.nombre, diaInicio, false, color))


                if(diaFinal >= now)
                    eventos.add(EventData(data.nombre, diaFinal, true, color))
            }
        }

        eventos.sortBy {
            it.fecha
        }

        if(eventos.size == 0){
            eventos.add(EventData("No hay eventos", null, null, "#00FFFFFF"))
        }

        Binder.restoreCallingIdentity(idToken)
    }

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews? {
        if (position == AdapterView.INVALID_POSITION || !::cursor.isInitialized || position >= eventos.size) {
            return null
        }

        val rv = RemoteViews(context.packageName, R.layout.widget_view_agenda_event_item)

        rv.setTextViewText(R.id.itemNombreEvento, eventos[position].nombre)
        if(eventos[position].fecha != null){
            rv.setTextViewText(R.id.itemDiaEvento, eventos[position].fecha!!.get(Calendar.DAY_OF_MONTH).toString())
            rv.setTextViewText(R.id.itemMesEvento, MES_COMPLETO[eventos[position].fecha!!.get(Calendar.MONTH)])
        }else{
            rv.setTextColor(R.id.itemNombreEvento, ContextCompat.getColor(context, R.color.colorPrimaryText))
        }
        rv.setInt(R.id.itemNombreEvento, "setBackgroundColor", Color.parseColor(eventos[position].color))

        if(eventos[position].isFinal == true){
            rv.setInt(R.id.itemInOut,"setBackgroundResource", R.drawable.ic_keyboard_arrow_up_black_24dp)
        }else if(eventos[position].isFinal == false){
            rv.setInt(R.id.itemInOut,"setBackgroundResource", R.drawable.ic_keyboard_arrow_down_black_24dp)
        }

        if(eventos[position].isFinal == null){
            rv.setViewVisibility(R.id.itemInOut, View.INVISIBLE)
        }else{
            rv.setViewVisibility(R.id.itemInOut, View.VISIBLE)
        }

        return rv
    }

    override fun getCount(): Int {
        return eventos.size
    }

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        if(::cursor.isInitialized){
            cursor.close()
        }

        if(::agendaEscolarDatabase.isInitialized){
            agendaEscolarDatabase.close()
        }
    }
}