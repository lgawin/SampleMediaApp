package dev.lgawin.android.sample.car

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.INDEX_UNSET
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import dev.lgawin.utils.media.MediaSessionServiceLogger
import dev.lgawin.utils.media.dump

class PlaybackService : MediaLibraryService() {

    private var mediaSession: MediaLibrarySession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        val context = this
        val player = createPlayer(context)
        val callback = createCallback(player)
        mediaSession = MediaLibrarySession.Builder(context, player, callback)
            .setId("LG-sample-session")
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
        Log.d(TAG, "onStartCommand: $intent, startId: $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controller: MediaSession.ControllerInfo): MediaLibrarySession? {
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
        private val TAG = "ForwardingPlayer"
        private val LOGD = false

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

    private val sampleMediaItem = MediaItem.Builder()
        .setMediaId("a6fc93b8-581e-47b9-85ce-7355e28f4e16")
        .setUri("https://s2.radio.co/s2b2b68744/listen")
        .setMediaMetadata(
            MediaMetadata.Builder()
                // required
                .setIsBrowsable(false)
                .setIsPlayable(true)
                // data for play controller
                .setTitle("Foo")
                .setArtist("Artist")
                .setArtworkUri(Uri.parse("https://i.pinimg.com/736x/63/a0/08/63a008f631ae7492a75a001bd0791e8f.jpg"))
                .build()
        )
        .build()

    private val samplePlaylistRoot = browsableItem(
        "bc67878c-8bc9-4dbf-9bf4-096c004ba571",
        "Root",
        MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS,
    )

    private fun playlistItem(uuid: String, name: String) = browsableItem(uuid, name, type = MediaMetadata.MEDIA_TYPE_PLAYLIST)

    private fun browsableItem(uuid: String, name: String, type: Int) = MediaItem.Builder()
        .setMediaId(uuid)
        .setMediaMetadata(
            MediaMetadata.Builder()
                // required
                .setIsBrowsable(true)
                .setIsPlayable(false)
                //
                .setTitle(name)
                .setMediaType(type)
                .build()
        )
        .build()

    private fun radioStation(uuid: String, name: String, uri: String) =
        mediaItem(uuid, name, uri, type = MediaMetadata.MEDIA_TYPE_RADIO_STATION)

    private fun mediaItem(uuid: String, name: String, uri: String, type: Int = MediaMetadata.MEDIA_TYPE_MUSIC) = MediaItem.Builder()
        .setMediaId(uuid)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                // required
                .setIsBrowsable(false)
                .setIsPlayable(true)
                //
                .setTitle(name)
                .setMediaType(type)
                .build()
        )
        .build()

    private val libraryRoot = samplePlaylistRoot
    private val tabs = listOf(
        TabItem(
            uuid = "a0a8fe48-fa00-4052-abb8-19d0b208e9a9",
            name = "Radios",
            items = listOf(
                radioStation(
                    uuid = "7d3b35f4-bce7-402c-b1c6-37cd206d8bbf",
                    name = "RMF FM",
                    uri = "http://195.150.20.242:8000/rmf_fm",
                ),
                radioStation(
                    uuid = "3b3afcb2-217f-4e22-a053-63063a162b9f",
                    name = "Radio ZET",
                    uri = "http://zet090-02.cdn.eurozet.pl:8404/",
                ),
                radioStation(
                    uuid = "c8d5c6a6-2cca-4485-9cae-2fd83d987ab7",
                    name = "Radio Złote Przeboje",
                    uri = "http://poznan7.radio.pionier.net.pl:8000/tuba9-1.mp3/",
                ),
            ),
        ),
        TabItem(
            uuid = "3ae2849d-a09e-455c-9388-ab5253ab693c",
            name = "Music",
            items = listOf(sampleMediaItem),
        ),
    )

    val allItems =
        tabs.flatMap { tab -> tab.items.map { it to tab.uuid }.plus(tabItem(tab) to null) }
            .associateBy { it.first.mediaId }

    private fun tabItem(tab: TabItem) = playlistItem(tab.uuid, tab.name)

    @OptIn(UnstableApi::class)
    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> {
        logger.onGetLibraryRoot(session, browser, params)

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

        if (parentId == libraryRoot.mediaId) {
            return LibraryResult.ofItemList(tabs.map { tabItem(it) }, null).immediately
        } else {
            val items = tabs.first { it.uuid == parentId }.items
            return LibraryResult.ofItemList(items, null).immediately
        }
        return super.onGetChildren(session, browser, parentId, page, pageSize, params)
    }

    // for setting media externally
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaItemsWithStartPosition> {
        logger.onPlaybackResumption(mediaSession, controller)

        return MediaItemsWithStartPosition(
            listOf(sampleMediaItem),
            0,
            0,
        ).immediately
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
            require(it.localConfiguration != null || it.mediaId == sampleMediaItem.mediaId)
        }

        player.setMediaItems(mediaItems.map { if (it.localConfiguration == null) sampleMediaItem else it })
        return MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs).immediately
    }

    private val <T> T.immediately
        get() = Futures.immediateFuture(this)
}

data class TabItem(val uuid: String, val name: String, val items: List<MediaItem>)
