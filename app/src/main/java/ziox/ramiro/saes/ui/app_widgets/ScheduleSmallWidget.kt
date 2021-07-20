package ziox.ramiro.saes.ui.app_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getCurrentClass
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class ScheduleSmallWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val rootView = RemoteViews(context.packageName, R.layout.widget_schedule_small)

        val db = LocalAppDatabase.invoke(context).scheduleRepository()
        val now = Hour.fromDate(Date()).toDouble().toInt()
        val scheduleList = db.getMySchedule()

        val range = scheduleList.getRangeBy { it.hourRange }

        if(scheduleList.isEmpty()){
            updateClassAndProgress(rootView, "Abre tu horario en la app para actualizar.", 0)
        }else{
            if(now !in range){
                updateClassAndProgress(rootView,"Fuera del horario", 0)
            }else{
                val currentClass = scheduleList.getCurrentClass()
                if(currentClass != null){
                    updateClassAndProgress(rootView, currentClass.className, now.minus(currentClass.hourRange.start.toDouble()).div(currentClass.hourRange.duration).toInt())
                }else{
                    updateClassAndProgress(rootView, "Tiempo libre", 0)
                }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, rootView)
    }

    private fun updateClassAndProgress(views : RemoteViews, msg : String, progress : Int){
        views.setTextViewText(R.id.materia_widget, msg)
        views.setProgressBar(R.id.progressBar_widget, 100, progress, false)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }
}

