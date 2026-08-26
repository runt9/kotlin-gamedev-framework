package com.runt9.kgdf.mcp.tool.output

import kotlinx.serialization.Serializable

/**
 * Anything a tool answers with instead of a result: nothing showing, no such action, the game declined.
 *
 * One shape rather than one per case, so a caller has a single thing to recognize and the difference is only
 * ever the message. There is no code alongside it because nothing branches on the kind, only reports it.
 */
@Serializable
data class HarnessError(val error: String)
