package ziox.ramiro.saes.ui.app_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.services.ListWidgetRemoteViewService

/**
 * Creado por Ramiro el 15/04/2019 a las 01:42 PM para SAESv2.
 */
class ScheduleMediumWidget  : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val rootView = RemoteViews(context.packageName, R.layout.widget_horario_list)
        val intent = Intent(context, ListWidgetRemoteViewService::class.java)
        val weekDay = WeekDay.todayByCalendar()
        val database = LocalAppDatabase.invoke(context).scheduleRepository()
        val scheduleList = database.getMySchedule()

        if(scheduleList.isEmpty()) {
            rootView.setTextViewText(R.id.horarioListDia, "Abre tu horario en la app para actualizar")
            rootView.setInt(R.id.horarioList, "setBackgroundResource", R.drawable.background_widget_list_empty_bg)
        }else {
            rootView.setTextViewText(R.id.horarioListDia, if (weekDay != WeekDay.UNKNOWN) {
                rootView.setInt(R.id.horarioList, "setBackgroundResource", android.R.color.transparent)
                weekDay.dayName
            } else {
                rootView.setInt(R.id.horarioList, "setBackgroundResource", R.drawable.background_widget_list_empty_bg)
                "Fin de semana"
            })
        }

        rootView.setRemoteAdapter(R.id.horarioList, intent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.horarioList)
        appWidgetManager.updateAppWidget(appWidgetId, rootView)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }
}

