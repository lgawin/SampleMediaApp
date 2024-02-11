package dev.lgawin.android.sample.car

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Rating
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.lgawin.utils.media.dump

@Suppress("FunctionName")
@OptIn(UnstableApi::class)
fun MediaLibrarySessionLogger(tag: String = "MediaLibrarySessionLogger") =
    object : MediaLibraryService.MediaLibrarySession.Callback {

        private val TAG = tag

        // MediaSession
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            Log.d(TAG, "onConnect: ")
            return super.onConnect(session, controller)
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            Log.d(TAG, "onPostConnect: ")
            super.onPostConnect(session, controller)
        }

        override fun onDisconnected(session: MediaSession, controller: MediaSession.ControllerInfo) {
            Log.d(TAG, "onDisconnected: ")
            super.onDisconnected(session, controller)
        }

        override fun onPlayerCommandRequest(session: MediaSession, controller: MediaSession.ControllerInfo, playerCommand: Int): Int {
            Log.d(TAG, "onPlayerCommandRequest: ")
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onSetRating(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaId: String,
            rating: Rating,
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "onSetRating: ")
            return super.onSetRating(session, controller, mediaId, rating)
        }

        override fun onSetRating(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            rating: Rating,
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "onSetRating: ")
            return super.onSetRating(session, controller, rating)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "onCustomCommand: ")
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            Log.d(TAG, "onAddMediaItems: ")
            return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.d(TAG, "onSetMediaItems: ")
            return super.onSetMediaItems(mediaSession, controller, mediaItems, startIndex, startPositionMs)
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.d(TAG, "onPlaybackResumption: ")
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onMediaButtonEvent(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, intent: Intent): Boolean {
            Log.d(TAG, "onMediaButtonEvent: ")
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        // MediaLibrarySession
        override fun onSubscribe(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<Void>> {
            Log.d(TAG, "onSubscribe: ")
            return super.onSubscribe(session, browser, parentId, params)
        }

        override fun onGetLibraryRoot(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "onGetLibraryRoot: id=${session.id}, browser=${browser.packageName}, params: ${params?.extras}")
            return super.onGetLibraryRoot(session, browser, params)
        }

        override fun onGetChildren(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.d(TAG, "onGetChildren: id=${session.id}, browser=${browser.packageName}, parentId=$parentId")
            return super.onGetChildren(session, browser, parentId, page, pageSize, params)
        }

        override fun onGetItem(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            Log.d(TAG, "onGetItem: ")
            return super.onGetItem(session, browser, mediaId)
        }

        override fun onSearch(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<Void>> {
            Log.d(TAG, "onSearch: ")
            return super.onSearch(session, browser, query, params)
        }

        override fun onGetSearchResult(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            Log.d(TAG, "onGetSearchResult: ")
            return super.onGetSearchResult(session, browser, query, page, pageSize, params)
        }

        override fun onUnsubscribe(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
        ): ListenableFuture<LibraryResult<Void>> {
            Log.d(TAG, "onUnsubscribe: ")
            return super.onUnsubscribe(session, browser, parentId)
        }
    }
