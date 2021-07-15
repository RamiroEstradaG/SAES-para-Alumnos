package ziox.ramiro.saes.ui.app_widgets

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService


class AgendaEscolarRemoteViewsFactory (val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    /*private lateinit var agendaEscolarDatabase : AgendaDao
    private lateinit var events : List<AgendaEvent>
    private data class EventData(val nombre: String, val fecha : GregorianCalendar?, val isFinal : Boolean?, val color : String)
    private val eventos = ArrayList<EventData>()

    override fun onCreate() {
        agendaEscolarDatabase = AppLocalDatabase.getInstance(context).agendaDao()

        events = agendaEscolarDatabase.getAll()
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
        eventos.clear()

        val idToken = Binder.clearCallingIdentity()
        val now = Calendar.getInstance()

        events = agendaEscolarDatabase.getAll()
        val colores = context.resources.getStringArray(R.array.paletaHorario)
        val tipoEventos = context.resources.getStringArray(R.array.tipo_eventos)

        for (event in events){
            val inicio = event.start.split(" ")
            val final = event.finish.split(" ")

            val diaInicio = GregorianCalendar(inicio[2].toInt(), MES.indexOf(inicio[0].toUpperCase(Locale.ROOT)), inicio[1].toInt(),0,0)
            val diaFinal = GregorianCalendar(final[2].toInt(), MES.indexOf(final[0].toUpperCase(Locale.ROOT)), final[1].toInt(),0,0)

            val color = if(!event.isWorkingDay){
                "#CCCCCC"
            }else{
                val index = tipoEventos.indexOf(event.eventType)
                colores[if(index >= 0){
                    index
                }else{
                    colores.lastIndex
                }]
            }

            if (diaInicio == diaFinal) {
                if(diaInicio >= now)
                    eventos.add(EventData(event.title, diaInicio, null, color))
            } else {
                if(diaInicio >= now)
                    eventos.add(EventData(event.title, diaInicio, false, color))


                if(diaFinal >= now)
                    eventos.add(EventData(event.title, diaFinal, true, color))
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
        if (position == AdapterView.INVALID_POSITION || !::events.isInitialized || position >= eventos.size) {
            return null
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_view_agenda_event_item)

        remoteViews.setTextViewText(R.id.itemNombreEvento, eventos[position].nombre)
        if(eventos[position].fecha != null){
            remoteViews.setTextViewText(R.id.itemDiaEvento, eventos[position].fecha!!.get(Calendar.DAY_OF_MONTH).toString())
            remoteViews.setTextViewText(R.id.itemMesEvento, MES_COMPLETO[eventos[position].fecha!!.get(Calendar.MONTH)])
        }else{
            remoteViews.setTextColor(R.id.itemNombreEvento, ContextCompat.getColor(context, R.color.colorTextPrimary))
        }
        remoteViews.setInt(R.id.itemNombreEvento, "setBackgroundColor", Color.parseColor(eventos[position].color))

        if(eventos[position].isFinal == true){
            remoteViews.setInt(R.id.itemInOut,"setBackgroundResource", R.drawable.ic_keyboard_arrow_up_black_24dp)
        }else if(eventos[position].isFinal == false){
            remoteViews.setInt(R.id.itemInOut,"setBackgroundResource", R.drawable.ic_keyboard_arrow_down_black_24dp)
        }

        if(eventos[position].isFinal == null){
            remoteViews.setViewVisibility(R.id.itemInOut, View.INVISIBLE)
        }else{
            remoteViews.setViewVisibility(R.id.itemInOut, View.VISIBLE)
        }

        return remoteViews
    }

    override fun getCount(): Int {
        return eventos.size
    }

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {}*/
    override fun onCreate() {
        TODO("Not yet implemented")
    }

    override fun onDataSetChanged() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getViewAt(position: Int): RemoteViews {
        TODO("Not yet implemented")
    }

    override fun getLoadingView(): RemoteViews {
        TODO("Not yet implemented")
    }

    override fun getViewTypeCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun hasStableIds(): Boolean {
        TODO("Not yet implemented")
    }
}