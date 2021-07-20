package ziox.ramiro.saes.data.models

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.screens.MainActivity


@SuppressLint("UnspecifiedImmutableFlag")
class Notification (context: Context, title: String?, body : String?, redirect: String = "") {
    private val notificationManager : NotificationManager
    private val notificationBuilder : NotificationCompat.Builder

    companion object{
        const val NOTIFICATION_ID_MESSAGING_SERVICE =   0b0000001
    }

    init {
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if(redirect.isNotBlank()){
            intent.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, redirect)
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "6"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                "notificaciones_general",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = "Notificaciones generales"
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 50, 50, 50, 50, 50)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))
            .setContentTitle(title)
            .setSound(defaultSound)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setColor(0x6C1D45)
            .setAutoCancel(true)
    }

    fun sendNotification(id: Int){
        notificationManager.notify(id, notificationBuilder.build())
    }
}