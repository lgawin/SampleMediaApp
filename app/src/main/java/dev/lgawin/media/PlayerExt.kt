package dev.lgawin.media

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

fun Player.playUri(uri: String) {
    setMediaItem(MediaItem.fromUri(uri))
}
