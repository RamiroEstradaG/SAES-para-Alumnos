package ziox.ramiro.saes.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import ziox.ramiro.saes.R
import ziox.ramiro.saes.services.ListWidgetRemoteViewService
import ziox.ramiro.saes.sql.HorarioDatabase
import java.util.*

/**
 * Creado por Ramiro el 15/04/2019 a las 01:42 PM para SAESv2.
 */
class HorarioListWidget  : AppWidgetProvider() {
    private var diasName : Array<String> = arrayOf(
        "Lunes",
        "Martes",
        "Miercoles",
        "Jueves",
        "Viernes"
    )

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_horario_list)
        val intent = Intent(context, ListWidgetRemoteViewService::class.java)
        val dia = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2
        val database = HorarioDatabase(context)
        database.createTable()

        if(database.getAll().count == 0) {
            views.setTextViewText(R.id.horarioListDia, "Abre tu horario en la app para actualizar")
            views.setInt(R.id.horarioList, "setBackgroundResource", R.drawable.background_widget_list_empty_bg)
        }else {
            views.setTextViewText(
                R.id.horarioListDia, if (dia in 0..4) {
                    views.setInt(R.id.horarioList, "setBackgroundResource", android.R.color.transparent)
                    diasName[dia]
                } else {
                    views.setInt(R.id.horarioList, "setBackgroundResource", R.drawable.background_widget_list_empty_bg)
                    "Fin de semana"
                }
            )
        }
        database.close()

        views.setRemoteAdapter(R.id.horarioList, intent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.horarioList)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }
}

