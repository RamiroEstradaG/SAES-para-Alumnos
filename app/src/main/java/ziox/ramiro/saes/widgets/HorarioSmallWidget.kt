package ziox.ramiro.saes.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ziox.ramiro.saes.R
import ziox.ramiro.saes.services.SmallWidgetUpdateService
import ziox.ramiro.saes.databases.CorreccionHorarioDatabase
import ziox.ramiro.saes.databases.HorarioDatabase
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

    private fun analiceHoras(all : Cursor, context: Context){
        val correccion = CorreccionHorarioDatabase(context)

        all.moveToPosition(-1)

        while(all.moveToNext()){
            val data = HorarioDatabase.cursorAsClaseData(all)
            val corrData = correccion.searchData(data)
            if (corrData != null){
                if(corrData.horaInicio < horaInicio) horaInicio = corrData.horaInicio
                if(corrData.horaFinal > horaFinal) horaFinal = corrData.horaFinal
            }else{
                if(data.horaInicio < horaInicio) horaInicio = data.horaInicio
                if(data.horaFinal > horaFinal) horaFinal = data.horaFinal
            }
        }

        correccion.close()
        all.moveToPosition(-1)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_horario_min)
        val calendar = Calendar.getInstance()
        val horarioDb = HorarioDatabase(context)
        val now = calendar.get(Calendar.HOUR_OF_DAY)+(calendar.get(Calendar.MINUTE)/60.0)
        horarioDb.createTable()
        val all = horarioDb.getAll()

        analiceHoras(all, context)

        if(all.count == 0){
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
        all.close()

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getMateriaActual(context : Context, all : Cursor, now : Double) : Pair<String?, Int>{
        val calendar = Calendar.getInstance()
        val correccionHorarioDatabase = CorreccionHorarioDatabase(context)
        var materia : String? = null
        var falta = 0

        while(all.moveToNext()){
            val dataHorario = HorarioDatabase.cursorAsClaseData(all)
            val data = correccionHorarioDatabase.searchData(dataHorario)?:dataHorario

            if(data.diaIndex == calendar.get(Calendar.DAY_OF_WEEK) - 2){
                if(now in data.horaInicio..data.horaFinal-(1/3600f)){
                    materia = data.materia
                    falta = ((now-data.horaInicio)/(data.horaFinal-data.horaInicio)*100f).toInt()
                    break
                }
            }
        }

        correccionHorarioDatabase.close()

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

