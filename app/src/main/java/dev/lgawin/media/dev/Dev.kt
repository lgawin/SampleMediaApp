package dev.lgawin.media.dev

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaBrowser
import dev.lgawin.media.dev.utils.dump
import kotlinx.coroutines.guava.await

object DevUseCases {
    const val TAG = "gawluk"

    sealed interface MediaTreeItem {

        data class BrowsableItem(val item: MediaItem) : MediaTreeItem {
            override fun toString(): String {
                return "BrowsableItem(${item.dump()})"
            }
        }

        data class PlayableItem(val item: MediaItem) : MediaTreeItem {
            override fun toString(): String {
                return "PlayableItem(${item.dump()})"
            }
        }
    }

    class BrowseMedia {

        suspend operator fun invoke(browser: MediaBrowser) {
            val root = browser.getRootItem()
            Log.d(TAG, "root: $root")
            val tabs = browser.getChildrenItems(root)
            Log.d(TAG, "tabs: $tabs")
            require(tabs.size < 4)
            tabs.forEach {
                require(it is MediaTreeItem.BrowsableItem)

                val items = browser.getChildrenItems(it)
                Log.d(TAG, "${it.item.mediaMetadata.title} => items: $items")
                items.forEach {
                    require(it is MediaTreeItem.PlayableItem)
                }
            }
        }
    }

    interface MediaTree {

        val root: MediaItem
        fun getById(mediaId: String): MediaItem?
        fun getChildren(parentId: String): List<MediaItem>
    }

    class ProvideMediaTreeUseCase {

        private data class TabItem(val uuid: String, val name: String, val items: List<MediaItem>)

        private val sampleMediaItem = mediaItem("a6fc93b8-581e-47b9-85ce-7355e28f4e16", "https://s2.radio.co/s2b2b68744/listen") {
            setTitle("Foo")
            setArtist("Artist")
            setArtworkUri(Uri.parse("https://i.pinimg.com/736x/63/a0/08/63a008f631ae7492a75a001bd0791e8f.jpg"))
        }

        private val samplePlaylistRoot = browsableItem(
            "bc67878c-8bc9-4dbf-9bf4-096c004ba571",
            "Root",
            MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS,
        )

        private fun mediaItem(uuid: String, uri: String?, configure: MediaMetadata.Builder.() -> Unit = {}) = MediaItem.Builder()
            .setMediaId(uuid)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    // required
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    // data for play controller
                    .apply(configure)
                    .build()
            )
            .build()

        private fun playlistItem(uuid: String, name: String) = browsableItem(uuid, name, type = MediaMetadata.MEDIA_TYPE_PLAYLIST)

        private fun browsableItem(uuid: String, name: String, type: Int) = mediaItem(uuid, null) {
            setIsBrowsable(true)
            setIsPlayable(false)
            setTitle(name)
            setMediaType(type)
        }

        private fun radioStation(uuid: String, name: String, src: String, artWork: String? = null) =
            mediaItem(uuid, name, src, type = MediaMetadata.MEDIA_TYPE_RADIO_STATION) {
                artWork?.let { setArtworkUri(Uri.parse(it)) }
            }

        private fun mediaItem(
            uuid: String,
            name: String,
            uri: String,
            type: Int = MediaMetadata.MEDIA_TYPE_MUSIC,
            configure: MediaMetadata.Builder.() -> Unit = {},
        ) = mediaItem(uuid, uri) {
            setTitle(name)
            setMediaType(type)
            configure(this)
        }

        private val libraryRoot = samplePlaylistRoot
        private val tabs = listOf(
            TabItem(
                uuid = "a0a8fe48-fa00-4052-abb8-19d0b208e9a9",
                name = "Radios",
                items = listOf(
                    radioStation(
                        uuid = "7d3b35f4-bce7-402c-b1c6-37cd206d8bbf",
                        name = "RMF FM",
                        src = "http://195.150.20.242:8000/rmf_fm",
                        artWork = "https://www.rmf.fm/inc/img/rmf-fm-logo.jpg",
                    ),
                    radioStation(
                        uuid = "3b3afcb2-217f-4e22-a053-63063a162b9f",
                        name = "Radio ZET",
                        src = "http://zet090-02.cdn.eurozet.pl:8404/",
                        artWork = "https://prowly-uploads.s3.eu-west-1.amazonaws.com/uploads/4587/assets/32616/Logo_RadioZET_red_2017_RGB.jpg",
                    ),
                    radioStation(
                        uuid = "c8d5c6a6-2cca-4485-9cae-2fd83d987ab7",
                        name = "Radio Złote Przeboje",
                        src = "http://poznan7.radio.pionier.net.pl:8000/tuba9-1.mp3/",
                        artWork = "https://myradioonline.pl/public/uploads/radio_img/radio-zlote-przeboje/play_250_250.jpg",
                    ),
                    radioStation(
                        uuid = "9ec3b93b-d511-434d-97e5-9bbdece47492",
                        name = "Radio Pogoda",
                        src = "http://stream13.radioagora.pl/tuba38-1.mp3",
                        artWork = "https://static.wirtualnemedia.pl/media/top/RadioPogoda_logo655.png",
                    ),
                ),
            ),
            TabItem(
                uuid = "3ae2849d-a09e-455c-9388-ab5253ab693c",
                name = "Music",
                items = listOf(sampleMediaItem),
            ),
        )

        val allItems =
            tabs.flatMap { tab -> tab.items.map { it to tab.uuid }.plus(tabItem(tab) to null) }
                .associateBy { it.first.mediaId }

        private fun tabItem(tab: TabItem) = playlistItem(tab.uuid, tab.name)


        operator fun invoke() = object : MediaTree {

            override val root: MediaItem
                get() = libraryRoot

            override fun getById(mediaId: String): MediaItem? =
                allItems[mediaId]?.first

            override fun getChildren(parentId: String): List<MediaItem> =
                if (parentId == libraryRoot.mediaId) tabs.map { tabItem(it) }
                else tabs.first { it.uuid == parentId }.items
        }
    }

    private suspend fun MediaBrowser.getRootItem(): MediaTreeItem.BrowsableItem =
        MediaTreeItem.BrowsableItem(getLibraryRoot(null).await().value!!)

    private suspend fun MediaBrowser.getChildrenItems(parent: MediaTreeItem.BrowsableItem): List<MediaTreeItem> {
        val children = getChildren(parent.item.mediaId, 0, 10, null).await().value
        return children?.map {
            when {
//                it.mediaMetadata.isPlayable == true -> MediaTreeItem.PlayableItem(it)
                it.mediaMetadata.isBrowsable == true -> MediaTreeItem.BrowsableItem(it)
                else -> MediaTreeItem.PlayableItem(it)
//                else -> MediaTreeItem.BrowsableItem(it)
            }
        } ?: emptyList()
    }
}
