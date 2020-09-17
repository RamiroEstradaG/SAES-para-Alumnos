package ziox.ramiro.saes.services

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import ziox.ramiro.saes.databases.AgendaEscolarDatabase
import ziox.ramiro.saes.databases.Evento
import ziox.ramiro.saes.databases.getAllEventosSiguientes
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.widgets.AgendaEscolarWidget
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(getPreference(context, "calendario_trabajo_notification", false)){
            getAllEventosSiguientes(context){
                updateDatabase(context, it)

                if (it.isEmpty()) return@getAllEventosSiguientes

                if(it.size == 1){
                    Notification(context, "Calendario de trabajo", "Para mañana: ${it.first().titulo}", "nav_calendario_trabajo")
                        .sendNotification(Notification.NOTIFICATION_ID_CALENDARIO_TRABAJO)
                    return@getAllEventosSiguientes
                }

                val tomorrow = Calendar.getInstance()
                tomorrow.add(Calendar.DATE, 2)
                tomorrow.set(Calendar.HOUR_OF_DAY, 0)
                tomorrow.set(Calendar.MINUTE, 0)
                tomorrow.set(Calendar.SECOND, 0)

                val grupos = it.filter {  filter ->
                    filter.dia < tomorrow.timeInMillis
                }.groupBy { ev ->
                    ev.tipo
                }.map { map->
                    when(map.key){
                        "Tarea" -> {
                            if(map.value.size > 1){
                                "${map.value.size} tareas"
                            }else{
                                "una tarea"
                            }
                        }
                        "Examen" -> {
                            if(map.value.size > 1){
                                "${map.value.size} examenes"
                            }else{
                                "un examen"
                            }
                        }
                        "Proyecto" -> {
                            if(map.value.size > 1){
                                "${map.value.size} proyectos"
                            }else{
                                "un proyecto"
                            }
                        }
                        "Recordatorio" -> {
                            if(map.value.size > 1){
                                "${map.value.size} recordatorios"
                            }else{
                                "un recordatorio"
                            }
                        }
                        "Evento" -> {
                            if(map.value.size > 1){
                                "${map.value.size} eventos"
                            }else{
                                "un evento"
                            }
                        }
                        else -> {
                            if(map.value.size > 1){
                                "${map.value.size} de otro tipo"
                            }else{
                                "uno de otro tipo"
                            }
                        }
                    }
                }

                Notification(context, "Calendario de trabajo", "Tienes " + grupos.joinToSentence() + " para mañana", "nav_calendario_trabajo")
                    .sendNotification(Notification.NOTIFICATION_ID_CALENDARIO_TRABAJO)
            }
        }else{
            Log.d("AlarmReceiver", "Receiver void")
        }
    }

    private fun updateDatabase(context: Context, eventos: List<Evento>){
        val agenda = AgendaEscolarDatabase(context)
        agenda.removeOnlyType(AgendaEscolarDatabase.TYPE_CALENDARIO_TRABAJO)

        for (e in eventos){
            val dia = Calendar.getInstance()
            dia.timeInMillis = e.dia
            val format = SimpleDateFormat("MMM dd yyyy", Locale("es", "MX")).format(dia.time)
            agenda.addEvento(
                AgendaEscolarDatabase.Data(
                    e.titulo,
                    AgendaEscolarDatabase.TYPE_CALENDARIO_TRABAJO,
                    e.tipo,
                    e.parent,
                    format,
                    format,
                    1
                ))
        }
        updateWidgets(context)
        agenda.close()
    }

    private fun updateWidgets(context: Context){
        val widgetAgenda = Intent(context, AgendaEscolarWidget::class.java)
        widgetAgenda.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idsAgenda = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, AgendaEscolarWidget::class.java))
        widgetAgenda.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsAgenda)
        context.sendBroadcast(widgetAgenda)
    }
}