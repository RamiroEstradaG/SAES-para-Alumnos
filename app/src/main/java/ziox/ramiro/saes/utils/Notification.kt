package ziox.ramiro.saes.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.MainActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.services.AlarmBootReceiver
import ziox.ramiro.saes.services.AlarmReceiver
import java.util.*


class Notification (context: Context, title: String?, body : String?, redirect: String = "") {
    private val notificationManager : NotificationManager
    private val notificationBuilder : NotificationCompat.Builder

    companion object{
        private const val ALARM_TYPE_RTC = 100
        private const val HOUR_ALARM = 16
        const val NOTIFICATION_ID_MESSAGING_SERVICE =   0b0000001
        const val NOTIFICATION_ID_CALENDARIO_TRABAJO =  0b0000010

        private fun enableBootReceiver(context: Context) {
            val receiver = ComponentName(context, AlarmBootReceiver::class.java)
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        fun scheduleRepeatingRTCNotification(context: Context) {
            val calendar = Calendar.getInstance()

            if (calendar.get(Calendar.HOUR_OF_DAY) >= HOUR_ALARM) {
                calendar.add(Calendar.DATE, 1)
            }

            calendar.set(Calendar.HOUR_OF_DAY, HOUR_ALARM)
            calendar.set(Calendar.MINUTE, 0)

            val intent = Intent(context, AlarmReceiver::class.java)

            val alarmIntentRTC = PendingIntent.getBroadcast(
                context,
                ALARM_TYPE_RTC,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManagerRTC = context.getSystemService(ALARM_SERVICE) as AlarmManager

            alarmManagerRTC.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntentRTC
            )

            enableBootReceiver(context)
        }
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
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification))
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