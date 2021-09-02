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
import androidx.core.content.ContextCompat
import ziox.ramiro.saes.R


class NotificationBuilder(val context: Context){
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private var title: String = ""
    private var message: String = ""
    private var pendingIntent: PendingIntent? = null
    private var channelId: String = ""
    private var channelName: String = ""
    private var channelDescription: String = ""

    init {
        setChannel(
            "general_notifications",
            "general_notifications",
            "Notificaciones generales"
        )
    }

    fun setTitle(title: String) = this.also {
        this.title = title
    }

    fun setDescription(message: String) = this.also {
        this.message = message
    }

    fun setChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ) = this.also {
        this.channelId = channelId
        this.channelName = channelName
        this.channelDescription = channelDescription

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = channelDescription
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 50, 50, 50, 50, 50)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun setPendingIntent(intent: Intent) = this.also {
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun buildAndNotify(notificationId: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground))
            .setContentTitle(title)
            .setSound(defaultSound)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(context, R.color.primary500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}