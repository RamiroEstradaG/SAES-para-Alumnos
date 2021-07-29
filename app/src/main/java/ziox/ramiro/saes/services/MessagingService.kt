package ziox.ramiro.saes.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ziox.ramiro.saes.data.models.Notification


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
    }
}