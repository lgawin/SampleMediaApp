package dev.lgawin.android.sample.car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.lgawin.android.sample.car.ui.theme.SampleCarAppTheme

class MainActivity : ComponentActivity() {

    private var player: Player? = null

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
            SampleCarAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        LazyColumn {
                            items(radios) { (name, uri) ->
                                TextButton(onClick = { player?.playFromUri(uri) }) {
                                    Text(text = name)
                                }
                            }
                        }
                        Button(
                            onClick = { player?.stop() },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(text = "Stop")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val context = this
        player = createPlayer(context)
    }

    override fun onStop() {
        super.onStop()
        player?.release()
    }

    private fun createPlayer(context: MainActivity) = ExoPlayer.Builder(context)
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
//            playWhenReady = true
            prepare()
        }
}

private fun Player.playFromUri(uri: String) {
    setMediaItem(MediaItem.fromUri(uri))
    play()
}
