package ziox.ramiro.saes.services

import android.content.Intent
import android.widget.RemoteViewsService
import ziox.ramiro.saes.ui.app_widgets.AgendaEscolarRemoteViewsFactory

class AgendaEscolarWidgetRemoteViewService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AgendaEscolarRemoteViewsFactory(applicationContext, intent)
    }

}