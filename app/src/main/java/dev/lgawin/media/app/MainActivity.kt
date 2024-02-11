package dev.lgawin.media.app

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.ui.PlayerControlView
import dev.lgawin.media.ui.theme.AppTheme
import dev.lgawin.media.dev.DevUseCases
import dev.lgawin.media.playUri
import dev.lgawin.media.usecases.ProvideMediaBrowserUseCase

class MainActivity : ComponentActivity() {

    private val provideMediaBrowser = ProvideMediaBrowserUseCase()
    private val browseMedia = DevUseCases.BrowseMedia()

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // taken from https://github.com/mikepierce/internet-radio-streams
        val radios = listOf(
            "RMF FM" to "http://195.150.20.242:8000/rmf_fm",
            "Radio Złote Przeboje" to "http://poznan7.radio.pionier.net.pl:8000/tuba9-1.mp3",
            "RMF Classic" to "http://rmfstream1.interia.pl:8000/rmf_classic",
            ////
            "Dublab" to "https://dublab.out.airtime.pro/dublab_a",
            "AmbientSleepingPill" to "http://radio.stereoscenic.com:80/asp-h.mp3",
            "Frisky-Chill" to "https://chill.friskyradio.com",
            "BadRadio" to "https://s2.radio.co/s2b2b68744/listen",
            "9128.live" to "https://streams.radio.co/s0aa1e6f4a/listen",
        )

        setContent {
            val context = LocalContext.current

            val browser by produceState<MediaBrowser?>(
                initialValue = null,
                producer = {
                    value = provideMediaBrowser(context)
                },
            )

            LaunchedEffect(browser) {
                if (browser == null) return@LaunchedEffect
                browser?.let { browseMedia(it) }
            }

            val player = browser
            DisposableEffect(Unit) {
                onDispose { player?.release() }
            }

            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(radios) { (name, uri) ->
                                TextButton(onClick = { player?.playUri(uri) }) {
                                    Text(text = name, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        player?.let {
                            PlayerControl(it)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerControl(player: Player, modifier: Modifier = Modifier) {
    Surface(color = Color.Black) {
        AndroidView(
            modifier = modifier
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
}
