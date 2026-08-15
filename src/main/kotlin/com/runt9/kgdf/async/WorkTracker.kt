package com.runt9.kgdf.async

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * Counts outstanding units of work so a caller can ask whether everything has settled.
 *
 * There is one rule: **call [enter] on the thread that submits the work, not inside the coroutine that will
 * later perform it.** Submitting is what a caller can see finish; the coroutine starting is not. Count too late
 * and the count reads zero in the gap between the two, which is exactly when something asks whether it is safe
 * to shut down.
 *
 * The same rule covers work that spawns more work: if a handler queues a follow-up, that [enter] happens while
 * the handler is still running, so the count never touches zero in between. That is what makes reaching zero
 * mean "finished" rather than "briefly quiet", and why no settling delay is needed anywhere.
 *
 * Safe to call from any thread.
 */
class WorkTracker {
    private val outstanding = MutableStateFlow(0)

    /** Exposed as a flow, not an `Int`, so several trackers can be composed into one settled-check without polling. */
    val pending: StateFlow<Int> = outstanding.asStateFlow()

    /** A plain volatile read, so this is safe on the rendering thread where suspending or blocking would deadlock. */
    val isIdle: Boolean get() = outstanding.value == 0

    // update {}, never `value = value + 1`: the latter is a non-atomic read-modify-write that drops concurrent
    // increments with no error.
    fun enter() = outstanding.update { it + 1 }

    fun exit() = outstanding.update { it - 1 }

    /** Returns immediately when already idle because `first` tests the current value before suspending, so there is no lost wakeup. */
    suspend fun awaitIdle() {
        outstanding.first { it == 0 }
    }

    /**
     * Zeroes the count and returns what was outstanding, for a consumer that has died and will not resume.
     *
     * Leaving a count stranded above zero is worse than admitting the work was lost: nothing would ever decrement
     * it again, so every later [awaitIdle] would hang forever.
     */
    fun reset(): Int = outstanding.getAndUpdate { 0 }
}
