package ziox.ramiro.saes.services

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ziox.ramiro.saes.data.models.NotificationBuilder
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.screens.MainActivity
import java.util.*


class MessagingService : FirebaseMessagingService() {
    companion object{
        const val NOTIFICATION_ID = 0
        const val CHANNEL_ID = "messaging"
        const val CHANNEL_NAME = "Notificaciones generales"
        const val CHANNEL_DESCRIPTION = "Notificaciones generales"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sendNotification(remoteMessage)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val redirect = remoteMessage.data["redirect"] ?: ""
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        if(redirect.isNotBlank()){
            intent.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, redirect)
        }

        NotificationBuilder(applicationContext)
            .setId(remoteMessage.messageId ?: Date().time.toString())
            .setChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_DESCRIPTION)
            .setTitle(remoteMessage.notification?.title ?: "")
            .setDescription(remoteMessage.notification?.body ?: "")
            .setPendingIntent(intent)
            .buildAndNotify(NOTIFICATION_ID)
    }



    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}