package dev.lgawin.android.sample.car

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import dev.lgawin.android.sample.car.ui.theme.SampleCarAppTheme

class MainActivity : ComponentActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // taken from https://github.com/mikepierce/internet-radio-streams
        val radios = listOf(
            "Dublab" to "https://dublab.out.airtime.pro/dublab_a",
            "AmbientSleepingPill" to "http://radio.stereoscenic.com:80/asp-h.mp3",
            "Frisky-Chill" to "https://chill.friskyradio.com",
            "BadRadio" to "https://s2.radio.co/s2b2b68744/listen",
            "9128.live" to "https://streams.radio.co/s0aa1e6f4a/listen",
        )

        setContent {
            val context = LocalContext.current
            val player = remember { createPlayer(context) }

            SampleCarAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(radios) { (name, uri) ->
                                TextButton(onClick = { player.setMediaUri(uri) }) {
                                    Text(text = name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        DisposableEffect(
                            AndroidView(
                                modifier = Modifier.fillMaxWidth(),
                                factory = {
                                    PlayerControlView(it).apply {
                                        this.player = player
                                        showTimeoutMs = -1
                                        setShowNextButton(true)
                                    }
                                },
                            )
                        ) {
                            onDispose { player.release() }
                        }
                    }
                }
            }

        }
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
}

private fun Player.setMediaUri(uri: String) {
    setMediaItem(MediaItem.fromUri(uri))
}
