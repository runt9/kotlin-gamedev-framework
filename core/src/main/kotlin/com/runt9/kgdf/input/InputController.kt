package com.runt9.kgdf.input

import com.runt9.kgdf.ext.lazyInject
import ktx.app.KtxInputAdapter

/**
 * Turns raw input callbacks into [InputAction] calls, looking them up in [InputTrackingService].
 *
 * **Registration with the `InputMultiplexer` is the entire scoping mechanism**: add this when a screen shows and
 * remove it on hide, and its actions are live exactly that long. There is no per-controller filter.
 *
 * Abstract so there is no concrete singleton to bind in `Injector` and share across screens. An empty subclass
 * is expected, and is where `keyTyped`, `mouseMoved` and `scrolled` get overridden.
 */
abstract class InputController : KtxInputAdapter {
    private val inputTracking by lazyInject<InputTrackingService>()

    override fun keyDown(keycode: Int) = dispatch(InputTrigger.DOWN, InputCode.Key(keycode), 0, 0, 0)

    override fun keyUp(keycode: Int) = dispatch(InputTrigger.UP, InputCode.Key(keycode), 0, 0, 0)

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) =
        dispatch(InputTrigger.DOWN, InputCode.Button(button), screenX, screenY, pointer)

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) =
        dispatch(InputTrigger.UP, InputCode.Button(button), screenX, screenY, pointer)

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int) =
        dispatch(InputTrigger.CANCELLED, InputCode.Button(button), screenX, screenY, pointer)

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) =
        runUntilConsumed(
            inputTracking.heldActionsFor(InputTrigger.DRAGGED),
            InputContext(screenX, screenY, pointer, null)
        )

    private fun dispatch(trigger: InputTrigger, code: InputCode, screenX: Int, screenY: Int, pointer: Int) =
        runUntilConsumed(inputTracking.actionsFor(trigger, code), InputContext(screenX, screenY, pointer, code))

    private fun runUntilConsumed(actions: List<InputAction>, ctx: InputContext) = actions.any { it.handle(ctx) }
}
