package ziox.ramiro.saes.services

import android.content.Intent
import android.widget.RemoteViewsService
import ziox.ramiro.saes.utils.ListWidgetRemoteViewsFactory

/**
 * Creado por Ramiro el 15/04/2019 a las 03:30 PM para SAESv2.
 */
class ListWidgetRemoteViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListWidgetRemoteViewsFactory(applicationContext, intent)
    }

}