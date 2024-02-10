package dev.lgawin.android.sample.car

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val context = this
        val player = createPlayer(context)
        mediaSession = MediaSession.Builder(context, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java),
                    FLAG_IMMUTABLE,
                )
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: $intent, startId: $startId")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controller: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "onGetSession: ${controller.dump()}")
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        // Check if the player is not ready to play or there are no items in the media queue
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            // Release the player
            player.release()
            // Release the MediaSession instance
            release()
            // Set _mediaSession to null
            mediaSession = null
        }
        // Call the superclass method
        super.onDestroy()
    }

    companion object {
        private const val TAG = "PlaybackService"
    }
}

private fun MediaSession.ControllerInfo.dump(): String {
    return packageName

}

private fun createPlayer(context: Context) = ExoPlayer.Builder(context)
    .setAudioAttributes(
        AudioAttributes.Builder()
//                    .setUsage(C.USAGE_MEDIA)
//                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build(),
        true, // handle audio focus
    )
    .setHandleAudioBecomingNoisy(true) // pause when headphones are disconnected
    .build()
    .apply {
        prepare()
    }
