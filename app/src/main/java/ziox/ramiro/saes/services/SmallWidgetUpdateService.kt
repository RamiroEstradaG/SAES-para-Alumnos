package ziox.ramiro.saes.services

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import ziox.ramiro.saes.widgets.HorarioSmallWidget

class SmallWidgetUpdateService(val context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        val widgetSmall = Intent(context, HorarioSmallWidget::class.java)
        widgetSmall.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idsSmall = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, HorarioSmallWidget::class.java))
        widgetSmall.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsSmall)
        context.sendBroadcast(widgetSmall)

        return Result.success()
    }
}
