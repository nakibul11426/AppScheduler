package com.nakibul.android.appscheduler.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nakibul.android.appscheduler.MainActivity
import com.nakibul.android.appscheduler.R
import com.nakibul.android.appscheduler.data.repository.ScheduleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundAppLauncherService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_launcher_channel"

        fun startService(context: Context, scheduleId: Long, packageName: String, appName: String) {
            val intent = Intent(context, ForegroundAppLauncherService::class.java).apply {
                putExtra("schedule_id", scheduleId)
                putExtra("package_name", packageName)
                putExtra("app_name", appName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    @Inject
    lateinit var repository: ScheduleRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("ForegroundService", "Service created")
        // Create and start foreground notification
        startForeground(NOTIFICATION_ID, createNotification("Launching scheduled app"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "Service started")
        val scheduleId = intent?.getLongExtra("schedule_id", -1L) ?: -1L
        val packageName = intent?.getStringExtra("package_name")
        val appName = intent?.getStringExtra("app_name") ?: "App"

        if (scheduleId != -1L && packageName != null) {
            Log.d("ForegroundService", "Launching app: $appName ($packageName)")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isExecuted = repository.isScheduleExecuted(scheduleId)

                    if (!isExecuted) {
                        // Update notification
                        val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification("Launching $appName")
                        )

                        // Launch the app
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                            startActivity(launchIntent)
                            Log.d("ForegroundService", "App launched: $packageName")

                            // Mark as executed
                            repository.markAsExecuted(scheduleId)
                        } else {
                            Log.e(
                                "ForegroundService",
                                "Failed to get launch intent for $packageName"
                            )
                        }
                    } else {
                        Log.d("ForegroundService", "Schedule $scheduleId already executed")
                    }
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Error launching app", e)
                } finally {
                    // Stop the service
                    stopSelf(startId)
                }
            }
        } else {
            Log.e("ForegroundService", "Invalid schedule ID or package name")
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private fun createNotification(contentText: String): Notification {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Launcher")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Launcher Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used to launch scheduled applications"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}