package com.runt9.kgdf.async

import kotlinx.coroutines.flow.Flow

/**
 * Outstanding asynchronous work a caller can poll or wait on. Several can be composed by [CombinedWorkSource].
 *
 * Count work when you accept it, not when you start running it. If handling one item queues another, the new one
 * must be counted before the first is marked done, or the count touches zero in between and anything waiting on
 * it concludes everything has finished. [WorkTracker] already does this.
 */
interface WorkSource {
    /** Must stay a non-suspending, non-blocking read: render-thread callers use this. */
    val isIdle: Boolean

    /** A [Flow], not a `StateFlow`, because a composed source has no scope to state it in. */
    val pending: Flow<Int>

    suspend fun awaitIdle()
}
