package com.auranote.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.auranote.app.MainActivity
import com.auranote.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service that owns the AudioRecorderManager and shows a persistent
 * "Recording in progress" notification with Pause / Stop actions.
 *
 * - microphone foregroundServiceType keeps recording alive when the app is
 *   minimized or the screen is locked.
 * - Audio focus is tracked here so phone calls / voice assistants pause us.
 * - A WakeLock guarantees the CPU stays awake while recording long meetings.
 */
@AndroidEntryPoint
class RecordingService : Service() {

    companion object {
        private const val TAG = "RecordingService"
        private const val CHANNEL_ID = "auranote_recording"
        private const val NOTIFICATION_ID = 7711

        const val ACTION_START = "com.auranote.app.action.START_RECORDING"
        const val ACTION_PAUSE = "com.auranote.app.action.PAUSE_RECORDING"
        const val ACTION_RESUME = "com.auranote.app.action.RESUME_RECORDING"
        const val ACTION_STOP = "com.auranote.app.action.STOP_RECORDING"

        fun start(context: Context) = control(context, ACTION_START)
        fun pause(context: Context) = control(context, ACTION_PAUSE)
        fun resume(context: Context) = control(context, ACTION_RESUME)
        fun stop(context: Context) = control(context, ACTION_STOP)

        private fun control(context: Context, action: String) {
            val intent = Intent(context, RecordingService::class.java).setAction(action)
            if (action == ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    @Inject lateinit var recorderManager: AudioRecorderManager
    @Inject lateinit var audioFocusManager: AudioFocusManager

    private var wakeLock: PowerManager.WakeLock? = null
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() { fun getService(): RecordingService = this@RecordingService }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
        }
        return START_STICKY
    }

    private fun handleStart() {
        ensureChannel()
        startForegroundCompat(buildNotification(RecorderState.RECORDING))
        acquireWakeLock()
        audioFocusManager.request { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS -> handlePause()
                AudioManager.AUDIOFOCUS_GAIN -> handleResume()
            }
        }
        recorderManager.startRecording()
    }

    private fun handlePause() {
        recorderManager.pauseRecording()
        updateNotification(RecorderState.PAUSED)
    }

    private fun handleResume() {
        recorderManager.resumeRecording()
        updateNotification(RecorderState.RECORDING)
    }

    private fun handleStop() {
        recorderManager.stopRecording()
        audioFocusManager.abandon()
        releaseWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(state: RecorderState) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: RecorderState): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pauseResume = PendingIntent.getService(
            this, 1,
            Intent(this, RecordingService::class.java).setAction(
                if (state == RecorderState.RECORDING) ACTION_PAUSE else ACTION_RESUME
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 2,
            Intent(this, RecordingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = getString(R.string.notif_recording_title)
        val text = when (state) {
            RecorderState.RECORDING -> getString(R.string.notif_recording_text)
            RecorderState.PAUSED -> getString(R.string.notif_paused_text)
            else -> getString(R.string.notif_recording_text)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openIntent)
            .addAction(
                if (state == RecorderState.RECORDING) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play,
                if (state == RecorderState.RECORDING) getString(R.string.notif_pause)
                else getString(R.string.notif_resume),
                pauseResume
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notif_stop), stop)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notif_channel_recording),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.notif_channel_recording_desc)
                    setShowBadge(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "AuraNote::RecordingWakeLock"
            ).apply { setReferenceCounted(false); acquire(4 * 60 * 60 * 1000L) }
        } catch (e: Exception) { Log.e(TAG, "WakeLock failed", e) }
    }

    private fun releaseWakeLock() {
        try { wakeLock?.takeIf { it.isHeld }?.release() } catch (_: Exception) {}
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        audioFocusManager.abandon()
    }
}
