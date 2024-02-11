package dev.lgawin.utils.media

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService

@Suppress("FunctionName")
@OptIn(UnstableApi::class)
fun MediaSessionServiceLogger(tag: String = "MediaSessionServiceLogger") = object : MediaSessionService.Listener {

    private val TAG = tag

    override fun onForegroundServiceStartNotAllowedException() {
        Log.d(TAG, "onForegroundServiceStartNotAllowedException")
        super.onForegroundServiceStartNotAllowedException()
    }
}
