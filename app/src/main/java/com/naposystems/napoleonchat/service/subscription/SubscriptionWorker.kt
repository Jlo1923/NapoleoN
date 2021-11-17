package com.naposystems.napoleonchat.service.subscription

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.mainActivity.SHOW_ENTER_PIN
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import java.util.concurrent.TimeUnit

const val SUBSCRIPTION_CHANNEL_ID = "a1"

class SubscriptionWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val prefs = SharedPreferencesManager(context)
        val userCreatedAt = prefs.getLong(Constants.SharedPreferences.PREF_USER_CREATED_AT)
        val currentTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val currentDaySinceCreated = TimeUnit.SECONDS.toDays(currentTimeInSeconds - userCreatedAt)
        val subscriptionStatus =
            SubscriptionStatus.valueOf(
                prefs.getString(
                    Constants.SharedPreferences.SubscriptionStatus,
                    SubscriptionStatus.ACTIVE.name
                )
            )
        if (currentDaySinceCreated in 4..7 && subscriptionStatus != SubscriptionStatus.ACTIVE) {
            val title = context.getString(R.string.text_subscription_title)
            val content = when (subscriptionStatus) {
                SubscriptionStatus.FREE_TRIAL_DAY_4 -> context.getString(R.string.text_subscription_free_trial_fourth_day)
                else -> context.getString(R.string.text_subscription_partial_lock)
            }
            showNotification(title, content)
        }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel: NotificationChannel =
                NotificationChannel(
                    SUBSCRIPTION_CHANNEL_ID,
                    "Subscription",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, SUBSCRIPTION_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentText(content)
                .setContentIntent(createPendingIntent())
        val notification: Notification = notificationBuilder.build()
        notificationManager.notify(1, notification)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(SHOW_ENTER_PIN, true)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
    }
}