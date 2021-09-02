package ziox.ramiro.saes.ui.app_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.runOnDefaultThread

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
        CoroutineScope(Dispatchers.Default).launch {
            updateScheduleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private suspend fun updateScheduleWidget(context : Context, appWidgetManager : AppWidgetManager, appWidgetId : Int) = runOnDefaultThread {
        val db = LocalAppDatabase.invoke(context).scheduleRepository()
        val rootView = RemoteViews(context.packageName, R.layout.widget_schedule_large)
        val scheduleList = db.getMySchedule()
        val weekDay = WeekDay.today()
        val range = scheduleList.getRangeBy { it.scheduleDayTime }
        val hourHeight = context.getHourHeight(range, appWidgetId, appWidgetManager)

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
            val height = it.scheduleDayTime.duration.times(hourHeight).toInt()
            val paddingTop = (it.scheduleDayTime.start.toDouble().minus(range.first)).times(hourHeight).toInt()

            val classRemoteView = RemoteViews(context.packageName, R.layout.widget_schedule_class_item)
            classRemoteView.setInt(R.id.clase_view, "setHeight", height)
            classRemoteView.setViewPadding(R.id.clase_view_parent,0, paddingTop,0,0)

            classRemoteView.setInt(R.id.clase_view, "setBackgroundColor", Color(it.color.toULong()).toArgb())

            classRemoteView.setTextViewText(R.id.clase_view, it.className.getInitials())

            rootView.addView(scheduleDayLayouts[it.scheduleDayTime.weekDay.calendarDayIndex-1], classRemoteView)
        }

        appWidgetManager.updateAppWidget(appWidgetId, rootView)
    }

    private fun initSchedule(rootView: RemoteViews, context: Context, scheduleList: List<ClassSchedule>){
        val range = scheduleList.getRangeBy{it.scheduleDayTime}

        for (id in scheduleDayLayouts){
            rootView.removeAllViews(id)
        }

        rootView.removeAllViews(R.id.backgroundHorarioWidgetLayout)
        rootView.removeAllViews(R.id.horasHorarioWidgetLayout)

        for(i in range){
            val backgroundRemote = RemoteViews(context.packageName, R.layout.widget_schedule_background)
            rootView.addView(R.id.backgroundHorarioWidgetLayout, backgroundRemote)
        }

        for(i in range){
            val hourRemote = RemoteViews(context.packageName, R.layout.widget_schedule_hour_item)
            hourRemote.setTextViewText(R.id.widget_horas_item, "$i:00")
            hourRemote.setTextColor(R.id.widget_horas_item, ContextCompat.getColor(context, R.color.colorTextPrimary))
            rootView.addView(R.id.horasHorarioWidgetLayout, hourRemote)
        }

        rootView.setViewVisibility(R.id.progressBarWidgetLarge, View.GONE)
        if(scheduleList.isEmpty()){
            rootView.setViewVisibility(R.id.widget_not_found, View.VISIBLE)
        }else{
            rootView.setViewVisibility(R.id.widget_not_found, View.GONE)
        }
    }

    private fun Context.getHourHeight(range: IntRange, appWidgetId: Int, appWidgetManager: AppWidgetManager): Double {
        val difference = range.last - range.first
        val height = getLayoutHeight(appWidgetId, appWidgetManager)

        return height.div(difference.toDouble())
    }

    private fun Context.getLayoutHeight(appWidgetId: Int, appWidgetManager: AppWidgetManager) : Float{
        val leveling = UserPreferences.invoke(this).getPreference(PreferenceKeys.ScheduleWidgetLeveling, 0)
        val height = appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        val spPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
        val dpPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,height - 8f + leveling, resources.displayMetrics)
        return dpPixel-spPixel
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val views = RemoteViews(context.packageName, R.layout.widget_schedule_large)
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

