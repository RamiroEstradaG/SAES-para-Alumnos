package ziox.ramiro.saes.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ziox.ramiro.saes.utils.Notification

class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Notification.scheduleRepeatingRTCNotification(context)
        }
    }
}