package com.runt9.kgdf.async

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.log.kgdfLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ktx.async.KtxAsync

/**
 * A single-threaded work queue with a settled-check attached: submit items from anywhere, handle them one at a
 * time in submission order on a dedicated thread, and ask whether everything has drained.
 *
 * Pass [handleItem] as a named function reference rather than a lambda, so the construction site reads as what
 * the queue does:
 * ```
 * private val queue = AsyncWorkQueue<Metric<*>>(asyncFactory, "Metrics-Thread", ::sendMetric)
 * ```
 *
 * The context comes from [AsyncFactory] rather than `newSingleThreadAsyncContext`, which is what lets a test
 * substitute a scheduler and drain the queue deterministically. Building the context directly would work and
 * would silently give up that property.
 */
class AsyncWorkQueue<T>(
    asyncFactory: AsyncFactory,
    private val threadName: String,
    private val handleItem: suspend (T) -> Unit
) : Disposable, WorkSource {
    private val logger = kgdfLogger()
    private val context = asyncFactory.newAsyncContext(threadName)
    private val queue = Channel<T>(Channel.UNLIMITED)
    private val tracker = WorkTracker()
    private var loop: Job? = null

    override val pending: StateFlow<Int> get() = tracker.pending
    override val isIdle: Boolean get() = tracker.isIdle
    override suspend fun awaitIdle() = tracker.awaitIdle()

    /**
     * Accepts an item for handling. Never suspends and never blocks the caller. The channel is unbounded, so a
     * producer is never parked behind a slow handler.
     */
    fun submit(item: T) {
        // Counted here on the CALLING thread, before the item is dispatched. Counting inside the consumer instead
        // would leave a window where submit() has returned and the count still reads zero.
        tracker.enter()
        queue.trySend(item).onFailure {
            tracker.exit()
            // Debug, not warn: dispose() is the only thing that closes the channel, so this always means "after
            // dispose" -- routine at shutdown, a lifecycle bug during play, and indistinguishable from here. A
            // level nobody can act on without going and checking teaches people to skim warnings, so keep it
            // retrievable rather than loud.
            logger.debug { "$threadName dropped an item; queue is closed" }
        }
    }

    /** Idempotent: a queue that is already consuming stays on its existing loop rather than gaining a second one. */
    fun start() {
        if (loop?.isActive == true) return

        loop = KtxAsync.launch(context) {
            try {
                consume()
                // Reached only when the channel closes, so nothing will ever drain what is still counted.
                dropRemaining()
            } catch (e: CancellationException) {
                throw e // stop(): the queue is restartable, so the count stays as it is
            } catch (e: Exception) {
                logger.error(e) { "$threadName consumer failed and will not resume" }
                dropRemaining()
            }
        }
    }

    /**
     * Stops consuming. Deliberately leaves the count alone: stop means "stop processing", not "pretend the queued
     * work is gone", and [start] resumes against the same channel and the same count.
     */
    fun stop() {
        loop?.cancel()
        loop = null
    }

    /**
     * Once disposed, the queue cannot be used afterward. Unlike [stop], this zeroes the count, because once the
     * channel is closed and the thread is gone nothing can drain what is left, and a count stuck above zero
     * hangs every later [awaitIdle].
     */
    override fun dispose() {
        queue.close()
        stop()
        dropRemaining()
        (context as? Disposable)?.dispose()
    }

    private suspend fun consume() {
        // Not consumeEach, despite reading better: it cancels the channel on the way out, so a stop() while
        // consuming would leave start() resuming against a dead one. This does not spin when the queue is empty
        // either -- hasNext suspends, so the thread is released until an item arrives or the channel closes.
        for (item in queue) {
            try {
                handleItem(item)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Caught per item so one bad handler cannot end the loop. Letting it escape would leave the queue
                // silently dead for the rest of the process, accepting items that are never handled again.
                logger.error(e) { "$threadName handler failed; queue continues" }
            } finally {
                tracker.exit()
            }
        }
    }

    private fun dropRemaining() {
        val dropped = tracker.reset()
        if (dropped > 0) logger.error { "$threadName stopped with $dropped item(s) unhandled" }
    }
}
