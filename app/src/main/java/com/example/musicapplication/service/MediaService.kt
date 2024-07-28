package com.example.musicapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.musicapplication.MainActivity
import com.example.musicapplication.R

class MediaService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private var mMediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY_EVENT -> {
                onPlay(intent.getStringExtra(URL))
                createNotificationChannel()
                startForegroundService()
            }

            PAUSE_EVENT -> {
                onPause()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mMediaPlayer?.release()
        mMediaPlayer = null
        super.onDestroy()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        log("Ready to play")
        mediaPlayer?.start()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Media")
            .setContentText("Artist - Song Title")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(getContentIntent())
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onError(mediaPlayer: MediaPlayer?, p1: Int, p2: Int): Boolean {
        mMediaPlayer?.release()
        mMediaPlayer = null
        log("failed to play media song")
        return true
    }

    companion object {
        const val PLAY_EVENT = "play_song"
        const val PAUSE_EVENT = "pause_song"
        const val URL = "url"
        const val TAG = "MediaService"
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 9
        private val immutableFlag =
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private fun onPlay(url: String?) {
        url ?: return
        mMediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener(this@MediaService)
            setOnErrorListener(this@MediaService)
            prepareAsync()
        }
        log("Start playing")
    }

    private fun onPause() {
        mMediaPlayer?.pause()
        log("Pause playing")
    }
}