package dev.lgawin.media.usecases

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import dev.lgawin.media.service.PlaybackService
import dev.lgawin.media.dev.utils.MediaBrowserLogger
import kotlinx.coroutines.guava.await

class ProvideMediaBrowserUseCase {

    suspend operator fun invoke(context: Context): MediaBrowser {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val factory = MediaBrowser.Builder(context, token)
            .setListener(MediaBrowserLogger())
            .buildAsync()
        return factory.await()
    }
}
