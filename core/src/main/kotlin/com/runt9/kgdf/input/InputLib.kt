package com.runt9.kgdf.input

/**
 * One action, fully declared. Position in the registered list is precedence: of two actions sharing a code, the
 * earlier one runs first.
 */
data class InputRegistration(val action: InputAction, val trigger: InputTrigger, val codes: Set<InputCode>)

/**
 * Something a player can do, defined by the consuming game.
 *
 * An action may bind keys and buttons together, because `touchDragged` reports no button: a drag has to ask what
 * is held rather than being told.
 */
interface InputAction {
    /**
     * Whether the event was consumed. `true` skips the remaining actions for that code and, since
     * [InputController] returns this to the `InputMultiplexer`, every processor behind it.
     *
     * Defaulted for actions bound only so [InputTrackingService.isHeld] can resolve them, which never dispatch.
     */
    fun handle(ctx: InputContext): Boolean = false
}

/**
 * Which libGDX callback fires an action. Only callbacks carrying an [InputCode] get one, so `keyTyped`,
 * `mouseMoved` and `scrolled` are overridden on the [InputController] instead.
 */
enum class InputTrigger {
    DOWN,
    UP,
    CANCELLED,

    /** `touchDragged` reports no button, so these fire on held codes. That is what separates a left-drag from a right-drag. */
    DRAGGED
}

/**
 * Event data only. An action needing held state injects [InputTrackingService] rather than reading it here.
 *
 * [code] is null on [InputTrigger.DRAGGED], where held state selected the action instead.
 */
data class InputContext(
    val screenX: Int,
    val screenY: Int,
    val pointer: Int,
    val code: InputCode?
)
