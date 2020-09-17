package ziox.ramiro.saes.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.CorreccionHorarioDatabase
import ziox.ramiro.saes.databases.HorarioDatabase
import ziox.ramiro.saes.utils.getInitials
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class HorarioLargeWidget : AppWidgetProvider() {
    private val tituloHorario = intArrayOf(R.id.widget_lunes_title,
            R.id.widget_martes_title,
            R.id.widget_miercoles_title,
            R.id.widget_jueves_title,
            R.id.widget_viernes_title)

    private val matrizHorario = intArrayOf(R.id.widget_lunesly,
            R.id.widget_martesly,
            R.id.widget_miercly,
            R.id.widget_juevesly,
            R.id.widget_viernesly)

    private var horaInicio = 24.0
    private var horaFinal = 0.0

    private fun updateAppWidget(context : Context, appWidgetManager : AppWidgetManager, appWidgetId : Int) {
        val horarioDb = HorarioDatabase(context)
        val correccionHorarioDatabase = CorreccionHorarioDatabase(context)
        val height = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        val views = RemoteViews(context.packageName, R.layout.widget_horario_max)
        val calendar = Calendar.getInstance()
        val nivelacion = context.applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE).getInt("widget_nivel",0)

        views.setInt(R.id.limiteTextView, "setHeight",TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10f,context.resources.displayMetrics).toInt())

        horarioDb.createTable()
        correccionHorarioDatabase.createTable()
        val all = horarioDb.getAll()

        for(titulo in tituloHorario){
            views.setTextColor(titulo, ContextCompat.getColor(context, R.color.colorPrimaryText))
        }
        if(calendar.get(Calendar.DAY_OF_WEEK)-2 in 0..4){
            views.setTextColor(tituloHorario[calendar.get(Calendar.DAY_OF_WEEK)-2], ContextCompat.getColor(context, R.color.colorHighlight))
        }


        analiceHoras(all, context)
        val horas = horaFinal-horaInicio

        initHorario(views, context)

        for (id in matrizHorario){
            views.removeAllViews(id)
        }

        views.setViewVisibility(R.id.progressBarWidgetLarge, View.GONE)
        if(all.count == 0){
            views.setViewVisibility(R.id.widget_not_found, View.VISIBLE)
        }else{
            views.setViewVisibility(R.id.widget_not_found, View.GONE)
        }

        while(all.moveToNext()){
            val dataHorario = HorarioDatabase.cursorAsClaseData(all)
            val data = CorreccionHorarioDatabase(context).searchData(dataHorario) ?: dataHorario

            if(data.diaIndex in  0..4){
                val duracion : Double = data.horaFinal - data.horaInicio
                val iniciales = data.materia.getInitials()
                val clase = RemoteViews(context.packageName, R.layout.sample_horario_clase_item)

                clase.setInt(R.id.clase_view, "setHeight",(duracion*getLayoutHeight(height,8f, nivelacion,16f,context)/horas).toInt())
                clase.setViewPadding(R.id.clase_view_parent,0,((data.horaInicio-horaInicio)*getLayoutHeight(height,8f, nivelacion,16f,context)/horas).toInt(),0,0)

                clase.setInt(R.id.clase_view, "setBackgroundColor", Color.parseColor(data.color))

                clase.setTextViewText(R.id.clase_view, iniciales)

                views.addView(matrizHorario[data.diaIndex], clase)
            }
        }

        all.close()
        horarioDb.close()
        correccionHorarioDatabase.close()

        appWidgetManager.updateAppWidget(appWidgetId, views)
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

    private fun initHorario(views: RemoteViews, context: Context){
        views.removeAllViews(R.id.backgroundHorarioWidgetLayout)
        views.removeAllViews(R.id.horasHorarioWidgetLayout)

        for(i in 0 until (horaFinal-horaInicio).toInt()){
            val backgroundRemote = RemoteViews(context.packageName, R.layout.sample_widget_horario_background_item)
            views.addView(R.id.backgroundHorarioWidgetLayout, backgroundRemote)
        }

        for(i in 0 .. (horaFinal-horaInicio).toInt()){
            if(i < (horaFinal-horaInicio).toInt()){
                val horasRemote = RemoteViews(context.packageName, R.layout.sample_widget_horario_hora_item)
                horasRemote.setTextViewText(R.id.widget_horas_item, "${horaInicio.toInt()+i}:00")
                horasRemote.setTextColor(R.id.widget_horas_item, ContextCompat.getColor(context, R.color.colorPrimaryText))
                views.addView(R.id.horasHorarioWidgetLayout, horasRemote)
            }else{
                views.setTextViewText(R.id.limiteTextView, "${horaInicio.toInt()+i}:00")
                views.setTextColor(R.id.limiteTextView, ContextCompat.getColor(context, R.color.colorPrimaryText))
            }
        }
    }

    private fun getLayoutHeight(height : Int, padding : Float, nivel : Int, sp : Float, context: Context) : Float{
        val spPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,context.resources.displayMetrics)
        val dpPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,height-(padding*2f)+nivel-1,context.resources.displayMetrics)
        return dpPixel-spPixel
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val views = RemoteViews(context.packageName, R.layout.widget_horario_max)
        views.setViewVisibility(R.id.progressBarWidgetLarge, View.VISIBLE)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

