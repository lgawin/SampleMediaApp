package dev.lgawin.android.sample.car

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import dev.lgawin.utils.media.dump
import kotlinx.coroutines.guava.await

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

object DevUseCases {

    const val TAG = "gawluk"

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
