package com.azzan.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

/**
 * Service to stream and play Azaan audio in the background.
 */
class PrayerAudioService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "prayer_audio_channel"
    private val NOTIFICATION_ID = 202

    private val AZAAN_URL = "https://www.dropbox.com/scl/fi/j6dew0rs076nzcsw1829e/azaan?rlkey=lpwe42p61du8two48v51k9mdz&st=17gxld95&dl=1"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP_AZAAN") {
            stopPlayback()
        } else {
            startStreaming()
        }
        return START_NOT_STICKY
    }

    private fun startStreaming() {
        startForeground(NOTIFICATION_ID, createNotification("Streaming Azaan..."))

        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(AZAAN_URL)
                    setOnPreparedListener(this@PrayerAudioService)
                    setOnErrorListener(this@PrayerAudioService)
                    setOnCompletionListener { stopPlayback() }
                    prepareAsync() 
                }
            } else if (!mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            showToast("Failed to initialize stream")
            stopSelf()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("Azaan Playing"))
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        showToast("Streaming error. Check internet connection.")
        stopPlayback()
        return true
    }

    private fun stopPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        stopForeground(true)
        stopSelf()
    }

    private fun createNotification(statusText: String): Notification {
        val stopIntent = Intent(this, PrayerAudioService::class.java).apply {
            action = "STOP_AZAAN"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Masjid Azaan")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Azaan Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for the Azaan stream"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }
}