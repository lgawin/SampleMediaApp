package dev.lgawin.utils.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun MediaItem.dump(): String {
    return "mediaId= " + mediaId + ", localConfiguration= " + localConfiguration + "; " + this.mediaMetadata.dump()
}

private fun MediaMetadata.dump() = listOf(
    "mediaType" to mediaType,
    "title" to title,
    "subtitle" to subtitle
).joinToString(",\n") { it.first + "=" + it.second }
