package com.example.sleepTimer

import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import java.util.*
import java.util.concurrent.TimeUnit
import android.os.*

class SleepTimerService: Service() {
    companion object{
        var isTimerRunning = false

        val ACTION_START_TIMER = "ACTION_START_TIMER"
        val ACTION_STOP_TIMER = "ACTION_STOP_TIMER"
        val ACTION_EXTEND = "ACTION_EXTEND"
        val ACTION_EXTEND_LITTLE = "ACTION_EXTEND_LITTLE"

        val NOTIFICATION_ID = 1
    }

    var builder: NotificationCompat.Builder? = null
    var notificationLayout: RemoteViews? = null
    var lockTime = Calendar.getInstance()
    var timer = Timer()
    var refreshTimerTask = object: TimerTask(){
        override fun run(){
            refreshTimer()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action

            when (action) {
                ACTION_START_TIMER -> {
                    startForegroundService()
                }
                ACTION_STOP_TIMER -> {
                    stopForegroundService()
                }
                ACTION_EXTEND -> {
                    extendTimer()
                }
                ACTION_EXTEND_LITTLE -> {
                    extendLittleTimer()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    /* Used to build and start foreground service. */
    private fun startForegroundService() {
        createNotificationChannel(this, NotificationManagerCompat.IMPORTANCE_LOW, false, getString(R.string.app_name), "App notification channel") // 1

        val CHANNEL_ID = "$packageName-${getString(R.string.app_name)}"

        val extendIntent = Intent(this, SleepTimerService::class.java)
        extendIntent.action = ACTION_EXTEND
        val pendingExtendIntent = PendingIntent.getService(this, 0, extendIntent, 0)

        val extendLittleIntent = Intent(this, SleepTimerService::class.java)
        extendLittleIntent.action = ACTION_EXTEND_LITTLE
        val pendingExtendLittleIntent = PendingIntent.getService(this, 0, extendLittleIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Apply the layouts to the notification
        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.tile_icon)

        notificationLayout = RemoteViews(packageName, R.layout.noti_layout)
        notificationLayout?.setOnClickPendingIntent(R.id.extend, pendingExtendIntent)
        notificationLayout?.setOnClickPendingIntent(R.id.extend_little, pendingExtendLittleIntent)

        val notification = builder?.build()

        // Start foreground service.
        startForeground(NOTIFICATION_ID, notification)

        setTimer()
        isTimerRunning = true
    }

    private fun stopForegroundService() {
        refreshTimerTask.cancel()
        isTimerRunning = false

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }


    fun refreshTimer(){
        val leftTime = lockTime.timeInMillis - Calendar.getInstance().timeInMillis

        // 종료
        if(leftTime < 1000){
            val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()

            stopForegroundService()

            return
        }

        val leftHour = TimeUnit.MILLISECONDS.toHours(leftTime)
        val leftMinute = TimeUnit.MILLISECONDS.toMinutes(leftTime - leftHour * 60 * 60 * 1000).toInt()
        val leftSecond = TimeUnit.MILLISECONDS.toSeconds(leftTime - leftHour * 60 * 60 * 1000 - leftMinute * 60 * 1000).toInt()

        if(leftMinute == 5 && leftSecond == 0){
            toastHandler.sendEmptyMessage(0);
        }

        var leftTimeStr = ""
        if(leftHour > 0) leftTimeStr += "${leftHour}:${padZero(leftMinute)}:${padZero(leftSecond)} 남음"
        else {
            if(leftMinute > 0) leftTimeStr += "${leftMinute}분 "
            leftTimeStr += "${leftSecond}초 남음"
        }

        var extendTimeStr: String
        if(App.prefs.hour > 0) extendTimeStr = "+ ${padZero(App.prefs.hour)}:${padZero(App.prefs.minute)}"
        else extendTimeStr = "+ ${App.prefs.minute}분"

        notificationLayout?.setTextViewText(R.id.extend, extendTimeStr)
        notificationLayout!!.setTextViewText(R.id.left, leftTimeStr)
        builder!!.setCustomContentView(notificationLayout)
        builder!!.setContent(notificationLayout)

        val notification = builder!!.build()

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    fun setTimer(){
        val hour = App.prefs.hour
        val minute = App.prefs.minute

        refreshTimerTask = object: TimerTask(){
            override fun run(){
                refreshTimer()
            }
        }

        lockTime = Calendar.getInstance()
        lockTime.add(Calendar.HOUR_OF_DAY, hour)
        lockTime.add(Calendar.MINUTE, minute)

        timer.schedule(refreshTimerTask, 1000, 1000)
    }

    fun extendTimer(){
        val hour = App.prefs.hour
        val minute = App.prefs.minute

        lockTime.add(Calendar.HOUR_OF_DAY, hour)
        lockTime.add(Calendar.MINUTE, minute)

        if(hour > 0)
            Toast.makeText(this, "${hour}시간 ${minute}분 연장되었습니다.", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(this, "${minute}분 연장되었습니다.", Toast.LENGTH_LONG).show()
    }

    fun extendLittleTimer(){
        lockTime.add(Calendar.MINUTE, 10)

        Toast.makeText(this, "10분 연장되었습니다.", Toast.LENGTH_LONG).show()
    }

    private fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)

            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun padZero (number: Int): String {
        when(number < 10){
            true -> return "0${number}"
            false -> return "${number}"
        }
    }

    private val toastHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Toast.makeText(applicationContext, "5분 후 잠깁니다.", Toast.LENGTH_SHORT).show()
        }
    }
}