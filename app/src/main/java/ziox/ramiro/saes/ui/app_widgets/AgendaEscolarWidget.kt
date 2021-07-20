package ziox.ramiro.saes.ui.app_widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.services.AgendaEscolarWidgetRemoteViewService
import ziox.ramiro.saes.utils.MES_COMPLETO
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import java.util.*

class AgendaEscolarWidget : AppWidgetProvider(){
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_agenda)
        val intent = Intent(context, AgendaEscolarWidgetRemoteViewService::class.java)
        val calendar = Calendar.getInstance()

        val dia = calendar.get(Calendar.DAY_OF_MONTH)
        val mes = calendar.get(Calendar.MONTH)

        views.setTextViewText(R.id.agendaWidgetDia, dia.toString())
        views.setTextViewText(R.id.agendaWidgetMes, MES_COMPLETO[mes])

        val schoolUrl = UserPreferences(context).getPreference(PreferenceKeys.SchoolUrl, null)

        views.setInt(R.id.agendaWidgetLogo, "setImageResource", if (schoolUrl != null) {
            School.findSchoolByUrl(schoolUrl)!!.logoId
        } else {
            R.drawable.ic_logopoli
        })

        views.setRemoteAdapter(R.id.agendaList, intent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.agendaList)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}