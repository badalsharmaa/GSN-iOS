package com.example.getsafenowclient.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Reusable helper for top pagination (chat style).
 */
class PagingListHelper(
    private val scope: CoroutineScope,
    private val listState: LazyListState,
    private val onLoadMore: suspend () -> Unit,
    private val isLoading: () -> Boolean,
    private val itemsCountProvider: () -> Int
) {

    private var paging = false
    private var anchorIndex = 0
    private var anchorOffset = 0
    private var previousCount = 0

    fun enableTopPagination() {
        // 🔥 Trigger when the first visible item is the very first in the list
        scope.launch {
            listState.firstVisibleItemIndexFlow().collectLatest { index ->
                if (!paging && !isLoading() && listState.firstVisibleItemIndex == 0) {

                    paging = true

                    // Save scroll anchor
                    anchorIndex = listState.firstVisibleItemIndex
                    anchorOffset = listState.firstVisibleItemScrollOffset
                    previousCount = itemsCountProvider()

                    onLoadMore()
                }
            }
        }

        // 🔥 When new items arrive → restore scroll
        scope.launch {
            listState.observeItemCountChange().collectLatest { newCount ->
                if (paging && newCount > previousCount) {
                    val added = newCount - previousCount
                    val targetIndex = (anchorIndex + added).coerceAtLeast(0)

                    scope.launch {
                        listState.scrollToItem(targetIndex, anchorOffset)
                    }

                    paging = false
                }
            }
        }
    }
}

fun LazyListState.firstVisibleItemIndexFlow() = snapshotFlow {
    firstVisibleItemIndex
}

fun LazyListState.observeItemCountChange() = snapshotFlow {
    layoutInfo.totalItemsCount
}
