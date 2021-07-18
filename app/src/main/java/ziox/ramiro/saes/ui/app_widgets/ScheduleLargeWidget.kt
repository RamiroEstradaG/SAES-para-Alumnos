package ziox.ramiro.saes.ui.app_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.getInitials

/**
 * Implementation of App Widget functionality.
 */
class ScheduleLargeWidget : AppWidgetProvider() {
    private val scheduleDayTitleIds = intArrayOf(R.id.widget_lunes_title,
            R.id.widget_martes_title,
            R.id.widget_miercoles_title,
            R.id.widget_jueves_title,
            R.id.widget_viernes_title)

    private val scheduleDayLayouts = intArrayOf(R.id.widget_lunesly,
            R.id.widget_martesly,
            R.id.widget_miercly,
            R.id.widget_juevesly,
            R.id.widget_viernesly)

    private fun updateAppWidget(context : Context, appWidgetManager : AppWidgetManager, appWidgetId : Int) {
        val db = LocalAppDatabase.invoke(context).scheduleRepository()
        val height = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        val rootView = RemoteViews(context.packageName, R.layout.widget_horario_max)
        val levelingPreference = UserPreferences(context).getPreference(PreferenceKeys.ScheduleWidgetLeveling, 0)
        val scheduleList = db.getMySchedule()
        val weekDay = WeekDay.today()
        val range = scheduleList.getRangeBy { it.hourRange }

        rootView.setInt(R.id.limiteTextView, "setHeight",TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10f,context.resources.displayMetrics).toInt())

        for((i, titleId) in scheduleDayTitleIds.withIndex()){
            rootView.setTextColor(titleId, ContextCompat.getColor(context, if(weekDay.calendarDayIndex == i+1){
                R.color.colorPrimary
            }else{
                R.color.colorTextPrimary
            }))
        }

        initSchedule(rootView, context, scheduleList)

        scheduleList.forEach {
            val classRemoteView = RemoteViews(context.packageName, R.layout.sample_horario_clase_item)
            classRemoteView.setInt(R.id.clase_view, "setHeight", (it.hourRange.duration.times(getLayoutHeight(height, levelingPreference, context)).div(range.last - range.first)).toInt())
            classRemoteView.setViewPadding(R.id.clase_view_parent,0, range.first.minus(it.hourRange.start.toDouble().times(getLayoutHeight(height, levelingPreference, context).div(range.last - range.first))).toInt(),0,0)

            classRemoteView.setInt(R.id.clase_view, "setBackgroundColor", it.color.toInt())

            classRemoteView.setTextViewText(R.id.clase_view, it.className.getInitials())

            rootView.addView(scheduleDayLayouts[it.hourRange.weekDay.calendarDayIndex-1], classRemoteView)
        }

        appWidgetManager.updateAppWidget(appWidgetId, rootView)
    }

    private fun initSchedule(rootView: RemoteViews, context: Context, scheduleList: List<ClassSchedule>){
        val range = scheduleList.getRangeBy{it.hourRange}

        for (id in scheduleDayLayouts){
            rootView.removeAllViews(id)
        }

        rootView.removeAllViews(R.id.backgroundHorarioWidgetLayout)
        rootView.removeAllViews(R.id.horasHorarioWidgetLayout)

        for(i in range){
            val backgroundRemote = RemoteViews(context.packageName, R.layout.sample_widget_horario_background_item)
            rootView.addView(R.id.backgroundHorarioWidgetLayout, backgroundRemote)
        }

        for(i in range){
            if(i < range.last-range.first){
                val hourRemote = RemoteViews(context.packageName, R.layout.sample_widget_horario_hora_item)
                hourRemote.setTextViewText(R.id.widget_horas_item, "${range.first+i}:00")
                hourRemote.setTextColor(R.id.widget_horas_item, ContextCompat.getColor(context, R.color.colorTextPrimary))
                rootView.addView(R.id.horasHorarioWidgetLayout, hourRemote)
            }else{
                rootView.setTextViewText(R.id.limiteTextView, "${range.first+i}:00")
                rootView.setTextColor(R.id.limiteTextView, ContextCompat.getColor(context, R.color.colorTextPrimary))
            }
        }

        rootView.setViewVisibility(R.id.progressBarWidgetLarge, View.GONE)
        if(scheduleList.isEmpty()){
            rootView.setViewVisibility(R.id.widget_not_found, View.VISIBLE)
        }else{
            rootView.setViewVisibility(R.id.widget_not_found, View.GONE)
        }
    }

    private fun getLayoutHeight(height : Int, leveling : Int, context: Context) : Float{
        val spPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, context.resources.displayMetrics)
        val dpPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,height-8f+leveling-1,context.resources.displayMetrics)
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

