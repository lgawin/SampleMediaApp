package dev.lgawin.android.sample.car

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.INDEX_UNSET
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.lgawin.media.dev.DevUseCases
import dev.lgawin.media.dev.utils.MediaLibrarySessionLogger
import dev.lgawin.media.dev.utils.MediaSessionServiceLogger
import dev.lgawin.media.dev.utils.dump
import java.util.UUID

class PlaybackService : MediaLibraryService() {

    private var mediaSession: MediaLibrarySession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        if (LOGD) Log.d(TAG, "onCreate")
        super.onCreate()
        val context = this
        val player = createPlayer(context)
        val callback = createCallback(player)
        mediaSession = MediaLibrarySession.Builder(context, player, callback)
            .setId(UUID.randomUUID().toString())
            .setSessionActivity(
                PendingIntent.getActivity(
                    context,
                    1,
                    Intent(context, MainActivity::class.java),
                    FLAG_IMMUTABLE,
                )
            )
            .build()
        setListener(MediaSessionServiceLogger())
        addSession(mediaSession!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (LOGD) Log.d(TAG, "onStartCommand: $intent, startId: $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controller: MediaSession.ControllerInfo): MediaLibrarySession? {
        if (LOGD) Log.d(TAG, "onGetSession: ${controller.dump()}")
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (LOGD) Log.d(TAG, "onTaskRemoved: $rootIntent")
        val player = mediaSession?.player ?: return
        // Check if the player is not ready to play or there are no items in the media queue
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service
            stopSelf()
        }
    }

    override fun onDestroy() {
        if (LOGD) Log.d(TAG, "onDestroy")
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
        private const val LOGD = false
        private const val TAG = "PlaybackService"
    }
}

@OptIn(UnstableApi::class)
private fun createPlayer(context: Context): Player {

    val exoPlayer = ExoPlayer.Builder(context)
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

    return object : ForwardingPlayer(exoPlayer) {
        private val LOGD = false
        private val TAG = "ForwardingPlayer"

        override fun getAvailableCommands(): Player.Commands {
            if (LOGD) Log.d(TAG, "getAvailableCommands")
            return super.getAvailableCommands().buildUpon()
//                .remove(COMMAND_PLAY_PAUSE)
//                .remove(COMMAND_SEEK_FORWARD)
//                .remove(COMMAND_SEEK_TO_NEXT)
//                .remove(COMMAND_SEEK_TO_PREVIOUS)
//                .add(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .build()
        }

        override fun isCommandAvailable(command: Int): Boolean {
            if (LOGD) Log.d(TAG, "isCommandAvailable: $command")
            return availableCommands.contains(command)
        }

        override fun getDuration(): Long {
            if (LOGD) Log.d(TAG, "getDuration")
            return TIME_UNSET
        }
    }
}

@OptIn(UnstableApi::class)
private fun createCallback(
    player: Player,
    logger: MediaLibraryService.MediaLibrarySession.Callback = MediaLibrarySessionLogger(),
) = object : MediaLibraryService.MediaLibrarySession.Callback by logger {

    private val TAG = "gawluk"

    private val provideMediaTree = DevUseCases.ProvideMediaTreeUseCase()
    private val mediaTree by lazy { provideMediaTree() }

    @OptIn(UnstableApi::class)
    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        logger.onGetLibraryRoot(session, browser, params)

        val libraryRoot = mediaTree.root
        Log.d(TAG, "onGetLibraryRoot: ${libraryRoot.dump()}")
        return LibraryResult.ofItem(libraryRoot, null).immediately
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        logger.onGetChildren(session, browser, parentId, page, pageSize, params)

        val mediaItems = mediaTree.getChildren(parentId)
        return LibraryResult.ofItemList(mediaItems, null).immediately
    }

    // for setting media externally
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaItemsWithStartPosition> {
        logger.onPlaybackResumption(mediaSession, controller)

        return MediaItemsWithStartPosition(listOf(), 0, 0).immediately
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaItemsWithStartPosition> {
        logger.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)

        require(startIndex == INDEX_UNSET)
        require(startPositionMs == TIME_UNSET)

        mediaItems.forEach {
            Log.d(TAG, "onSetMediaItems: ${it.dump()}")
        }

        val items = mediaItems.map {
            if (it.localConfiguration != null) it else (mediaTree.getById(it.mediaId) ?: it)
        }
        player.setMediaItems(items)
        return MediaItemsWithStartPosition(items, startIndex, startPositionMs).immediately
    }

    private val <T> T.immediately
        get() = Futures.immediateFuture(this)
}
