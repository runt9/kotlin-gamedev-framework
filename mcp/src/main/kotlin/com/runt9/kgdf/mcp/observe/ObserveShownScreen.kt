package com.runt9.kgdf.mcp.observe

import kotlinx.coroutines.withTimeout
import ktx.async.onRenderingThread
import kotlin.time.Duration.Companion.seconds

/** Generous: a timeout here means the render loop stopped, which is worth surfacing rather than waiting out. */
internal val RENDER_TIMEOUT = 10.seconds

/**
 * One hop onto the rendering thread, bounded. Everything the harness reads or drives goes through here, so an
 * answer cannot describe two different frames and a stalled render loop surfaces rather than hanging.
 */
internal suspend fun <T> renderHop(block: () -> T): T = withTimeout(RENDER_TIMEOUT) { onRenderingThread { block() } }
