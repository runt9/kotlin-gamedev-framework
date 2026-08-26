package com.runt9.kgdf.input

/**
 * Holds which inputs are currently held, and what the game's actions are.
 *
 * Held state comes from observed events rather than from polling the device, which is the point:
 * `Gdx.input.isKeyPressed` reports the real keyboard and mouse, so an event pushed through the
 * `InputMultiplexer` never changes what it says, and anything gated on it cannot be driven programmatically.
 *
 * Free of libGDX types, so it is unit-testable with no LibGDX application present. [InputTracker] feeds it and
 * [InputController] dispatches from it.
 *
 * Not thread-safe and does not need to be: input callbacks arrive on the rendering thread and every reader is an
 * input handler on that same thread.
 */
class InputTrackingService {
    private val heldKeys = mutableSetOf<Int>()
    private val heldButtons = mutableSetOf<Int>()

    /**
     * Every action the game has, in precedence order. This is the only place an action is declared, so nothing
     * else names a keycode and rebinding is an edit to this list alone.
     */
    private var registrations: List<InputRegistration> = emptyList()

    /**
     * Declares the game's actions. Call once at startup rather than at screen init: a registration made per
     * screen disappears with the screen, while the held state does not.
     */
    fun registerActions(registrations: List<InputRegistration>) {
        this.registrations = registrations
    }

    /** Actions for an arriving code, in precedence order. Used by every trigger except [InputTrigger.DRAGGED]. */
    fun actionsFor(trigger: InputTrigger, code: InputCode) =
        registrations.filter { it.trigger == trigger && code in it.codes }.map { it.action }

    /**
     * Actions selected by what is held rather than by an arriving code. Only [InputTrigger.DRAGGED] needs this,
     * since `touchDragged` reports no button.
     */
    fun heldActionsFor(trigger: InputTrigger) =
        registrations.filter { it.trigger == trigger && it.codes.any(::isCodeHeld) }.map { it.action }

    fun isHeld(action: InputAction) =
        registrations.any { it.action == action && it.codes.any(::isCodeHeld) }

    fun isHeld(code: InputCode) = isCodeHeld(code)

    /**
     * What is bound to an action, for a caller that has to produce the input rather than react to it. Keeps the
     * keymap the only place a code is named: a synthesized event asks what a real one would have carried.
     */
    fun codesFor(action: InputAction, trigger: InputTrigger) =
        registrations.filter { it.action == action && it.trigger == trigger }.flatMap { it.codes }

    /**
     * Whether any mouse button is down, which is not the same as any action being held: an action bound to a key
     * is held while a modifier alone is down. Deriving one from the other keeps a drag alive on a held modifier.
     */
    val anyButtonDown: Boolean get() = heldButtons.isNotEmpty()

    fun keyDown(keycode: Int) {
        heldKeys += keycode
    }

    fun keyUp(keycode: Int) {
        heldKeys -= keycode
    }

    fun buttonDown(button: Int) {
        heldButtons += button
    }

    fun buttonUp(button: Int) {
        heldButtons -= button
    }

    /**
     * Drops all held state, for focus loss and pause where the release events never arrive. Without it a drag
     * whose mouse-up happens outside the window leaves that button held forever, and there is no device to
     * reconcile against without reintroducing the polling this class exists to remove.
     */
    fun clear() {
        heldKeys.clear()
        heldButtons.clear()
    }

    private fun isCodeHeld(code: InputCode) = when (code) {
        is InputCode.Key -> code.keycode in heldKeys
        is InputCode.Button -> code.buttonId in heldButtons
    }
}
