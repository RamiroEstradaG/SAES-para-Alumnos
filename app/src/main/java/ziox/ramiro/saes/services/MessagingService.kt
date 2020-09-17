package ziox.ramiro.saes.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ziox.ramiro.saes.utils.Notification
import ziox.ramiro.saes.databases.updateToken


class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sendNotification(remoteMessage)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        Notification(applicationContext, remoteMessage.notification?.title, remoteMessage.notification?.body, remoteMessage.data["redirect"] ?: "")
            .sendNotification(Notification.NOTIFICATION_ID_MESSAGING_SERVICE)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        try {
            updateToken(this, p0)
        }catch (e : Exception){
            Log.e("AppException", e.toString())
        }
    }
}