package com.example.mobilecomp
//android:name=".NotificationApplication"

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


//constraints 23:30
//ajoitus 27:30
//final notif 43:00
class NotificationWorker(
    private val context: Context,
    private val worker_params: WorkerParameters
): CoroutineWorker(context, worker_params) {

    override suspend fun doWork(): Result {

        //setNotif("asd", context)
        //return Result.success()
        return try {
            Log.d("NotificationWorker", "Should this actually do smth useful")
            Result.success()
        } catch (e: java.lang.Exception) {
            Result.failure()
        }

    }
}


fun setNotif(msg: String, duration: Long, context: Context) {

    val workManager = WorkManager.getInstance(context)
    val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(duration, TimeUnit.MILLISECONDS)
        .build()

    workManager.enqueue(notificationWorker)

    workManager.getWorkInfoByIdLiveData(notificationWorker.id)
        .observeForever { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                succNotif(msg, context)
            }
        }
}

private fun succNotif(msg: String, context: Context) {
    val notifId = Random.nextInt()
    val builder = NotificationCompat.Builder(context, "reminder_channel")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("New reminder due")
        .setContentText(msg)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notifId, builder.build())
    }
}


class NotificationApplication(): Application() {

    override fun onCreate() {
        super.onCreate()
    }

    fun createChannel(context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "reminder_channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
