package ziox.ramiro.saes.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.net.toUri
import ziox.ramiro.saes.ui.app_widgets.ScheduleLargeWidget
import ziox.ramiro.saes.ui.app_widgets.ScheduleMediumWidget
import ziox.ramiro.saes.ui.app_widgets.ScheduleSmallWidget

fun Context.launchUrl(url: String?){
    if(!url.isNullOrBlank()){
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = url.toUri()
        })
    }
}

fun Context.isNetworkAvailable() : Boolean{
    if(UserPreferences.invoke(this).getPreference(PreferenceKeys.OfflineMode, false)){
        return false
    }

    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        }
    }

    return false
}


fun Context.updateWidgets() {
    val widgetLarge = Intent(this, ScheduleLargeWidget::class.java)
    widgetLarge.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val idsLarge = AppWidgetManager.getInstance(this)
        .getAppWidgetIds(ComponentName(this, ScheduleLargeWidget::class.java))
    widgetLarge.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsLarge)
    sendBroadcast(widgetLarge)

    val widgetList = Intent(this, ScheduleMediumWidget::class.java)
    widgetList.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val idsList = AppWidgetManager.getInstance(this)
        .getAppWidgetIds(ComponentName(this, ScheduleMediumWidget::class.java))
    widgetList.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsList)
    sendBroadcast(widgetList)

    val widgetAgenda = Intent(this, ScheduleSmallWidget::class.java)
    widgetAgenda.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val idsAgenda = AppWidgetManager.getInstance(this)
        .getAppWidgetIds(ComponentName(this, ScheduleSmallWidget::class.java))
    widgetAgenda.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsAgenda)
    sendBroadcast(widgetAgenda)
}