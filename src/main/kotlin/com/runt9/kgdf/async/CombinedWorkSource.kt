package com.runt9.kgdf.async

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Open and concrete so a consuming project can subclass it naming its own sources, which is what gives the
 * composite a distinct type for dependency injection to key on; direct vararg construction stays available.
 *
 * Correct only while every composed [WorkSource] holds the counting rule described there.
 */
open class CombinedWorkSource(private vararg val sources: WorkSource) : WorkSource {
    override val isIdle get() = sources.all { it.isIdle }

    override val pending: Flow<Int> = combine(sources.map { it.pending }) { counts -> counts.sum() }

    override suspend fun awaitIdle() {
        // Not just a fast path. combine() over no sources completes without emitting, so first {} throws there
        // rather than returning; this covers that case too. Deleting it costs a channel and a coroutine per
        // source on every already-settled call, which is the common one for a caller that polls.
        if (isIdle) return
        pending.first { it == 0 }
    }

    fun describePending() = sources.filterNot { it.isIdle }.joinToString(", ") { it::class.simpleName ?: "?" }
}
