package com.runt9.kgdf.input

/**
 * A single physical input, either a keyboard key or a mouse button.
 *
 * These cannot share one `Int` keyspace: libGDX numbers mouse buttons from 0 and keycodes also start at 0, so a
 * set of bare ints reports the left button held whenever key 0 is down. Distinct types make that collision
 * unrepresentable.
 */
sealed interface InputCode {
    @JvmInline
    value class Key(val keycode: Int) : InputCode

    @JvmInline
    value class Button(val buttonId: Int) : InputCode
}
