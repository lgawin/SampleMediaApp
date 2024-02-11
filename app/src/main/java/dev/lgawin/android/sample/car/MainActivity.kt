package dev.lgawin.android.sample.car

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerControlView
import dev.lgawin.android.sample.car.ui.theme.SampleCarAppTheme
import kotlinx.coroutines.guava.await

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
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val factory = MediaController.Builder(context, token).buildAsync()

            val player by produceState<Player?>(
                initialValue = null,
                producer = { value = factory.await() },
            )

            SampleCarAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(radios) { (name, uri) ->
                                TextButton(onClick = { player?.setMediaUri(uri) }) {
                                    Text(text = name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        if (player != null) {
                            Surface(color = Color.Black) {
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    factory = {
                                        PlayerControlView(it).apply {
                                            this.player = player
                                            showTimeoutMs = -1
                                        }
                                    },
                                )
                            }
                            DisposableEffect(Unit) {
                                onDispose { player?.release() }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Player.setMediaUri(uri: String) {
    setMediaItem(MediaItem.fromUri(uri))
}
