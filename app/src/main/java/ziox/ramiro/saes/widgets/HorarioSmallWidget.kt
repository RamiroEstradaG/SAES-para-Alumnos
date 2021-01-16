package ziox.ramiro.saes.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.ScheduleClass
import ziox.ramiro.saes.services.SmallWidgetUpdateService
import ziox.ramiro.saes.utils.getPreference
import ziox.ramiro.saes.utils.setPreference
import ziox.ramiro.saes.utils.toProperCase
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Implementation of App Widget functionality.
 */
class HorarioSmallWidget : AppWidgetProvider() {
    private var horaInicio = 24.0
    private var horaFinal = 0.0
    private lateinit var worker : PeriodicWorkRequest

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val interval = getPreference(context, "widget_small_interval", 15).toLong()
        if(interval < 15){
            setPreference(context, "widget_small_interval", 15)
        }
        worker = PeriodicWorkRequestBuilder<SmallWidgetUpdateService>(interval, TimeUnit.MINUTES).setConstraints(
            Constraints.Builder().setRequiresBatteryNotLow(true).build()
        ).build()

        WorkManager.getInstance(context).enqueue(worker)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        if(::worker.isInitialized){
            WorkManager.getInstance(context).cancelWorkById(worker.id)
        }
    }

    private fun analiceHoras(all : List<ScheduleClass>, context: Context){
        val correccion = AppLocalDatabase.getInstance(context).adjustedClassScheduleDao()

        for (clase in all){
            val corrData = correccion.get(clase.uid)
            if (corrData != null){
                if(corrData.startHour < horaInicio) horaInicio = corrData.startHour
                if(corrData.finishHour > horaFinal) horaFinal = corrData.finishHour
            }else{
                if(clase.startHour < horaInicio) horaInicio = clase.startHour
                if(clase.finishHour > horaFinal) horaFinal = clase.finishHour
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_horario_min)
        val calendar = Calendar.getInstance()
        val horarioDb = AppLocalDatabase.getInstance(context).originalClassScheduleDao()
        val now = calendar.get(Calendar.HOUR_OF_DAY)+(calendar.get(Calendar.MINUTE)/60.0)
        val all = horarioDb.getAll()

        analiceHoras(all, context)

        if(all.isEmpty()){
            setClaseAndProgress(views, "Abre tu horario en la app para actualizar.", 0)
        }else{
            if(now !in horaInicio..horaFinal || calendar.get(Calendar.DAY_OF_WEEK) !in Calendar.MONDAY..Calendar.FRIDAY){
                setClaseAndProgress(views,"Fuera de clase", 0)
            }else{
                val (materia, falta) = getMateriaActual(context, all, now)
                if(materia != null){
                    setClaseAndProgress(views,materia.toProperCase(), falta)
                }else{
                    setClaseAndProgress(views, "Tiempo libre", 0)
                }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getMateriaActual(context : Context, all : List<ScheduleClass>, now : Double) : Pair<String?, Int>{
        val calendar = Calendar.getInstance()
        val correccionHorarioDatabase = AppLocalDatabase.getInstance(context).adjustedClassScheduleDao()
        var materia : String? = null
        var falta = 0

        for(clase in all){
            val data = correccionHorarioDatabase.get(clase.uid) ?: clase

            if(data.dayIndex == calendar.get(Calendar.DAY_OF_WEEK) - 2){
                if(now in data.startHour..data.finishHour-(1/3600f)){
                    materia = data.courseName
                    falta = ((now-data.startHour)/(data.finishHour-data.startHour)*100f).toInt()
                    break
                }
            }
        }

        return Pair(materia, falta)
    }

    private fun setClaseAndProgress(views : RemoteViews,msg : String, progress : Int){
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

