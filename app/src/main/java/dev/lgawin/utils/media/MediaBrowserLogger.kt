package dev.lgawin.utils.media

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService

@Suppress("FunctionName")
@OptIn(UnstableApi::class)
fun MediaBrowserLogger(
    tag: String = "MediaBrowserLogger",
    loggingController: MediaController.Listener = MediaControllerLogger(tag),
): MediaBrowser.Listener = object : MediaBrowser.Listener, MediaController.Listener by loggingController {

    private val TAG = tag

    override fun onChildrenChanged(
        browser: MediaBrowser,
        parentId: String,
        itemCount: Int,
        params: MediaLibraryService.LibraryParams?,
    ) {
        Log.d(TAG, "onChildrenChanged: ")
        super.onChildrenChanged(browser, parentId, itemCount, params)
    }

    override fun onSearchResultChanged(
        browser: MediaBrowser,
        query: String,
        itemCount: Int,
        params: MediaLibraryService.LibraryParams?,
    ) {
        Log.d(TAG, "onSearchResultChanged: ")
        super.onSearchResultChanged(browser, query, itemCount, params)
    }
}
