package com.runt9.kgdf.input

import com.runt9.kgdf.inject.Injector
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import ktx.inject.Context

/**
 * The controller resolves its service through `lazyInject`, so these swap the global `Injector` context. The
 * `restoreContext` in `afterTest` is not optional: the container is process-global, and a leaked override
 * surfaces as an unrelated spec failing later.
 */
class InputControllerTest : FunSpec({
    val leftButton = InputCode.Button(0)
    val rightButton = InputCode.Button(1)

    /** Records that it ran, and reports whether it consumed. */
    class Recording(private val consumes: Boolean = false) : InputAction {
        val calls = mutableListOf<InputContext>()
        override fun handle(ctx: InputContext): Boolean {
            calls += ctx
            return consumes
        }
    }

    /** Registered but never overrides handle, the case a modifier hits. */
    val silent = object : InputAction {}

    class TestController : InputController()

    lateinit var service: InputTrackingService

    fun controllerWith(vararg registrations: InputRegistration): TestController {
        service = InputTrackingService().apply { registerActions(registrations.toList()) }
        Injector.overrideContext(Context().apply { bindSingleton(service) })
        return TestController()
    }

    afterTest { Injector.restoreContext() }

    test("an arriving code runs the action registered for that trigger") {
        val primary = Recording()
        val controller = controllerWith(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        controller.touchUp(12, 34, 0, 0)

        primary.calls.size shouldBe 1
        primary.calls.single().screenX shouldBe 12
        primary.calls.single().screenY shouldBe 34
        primary.calls.single().code shouldBe leftButton
    }

    test("the same code on a different trigger does not run") {
        val primary = Recording()
        val controller = controllerWith(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        controller.touchDown(0, 0, 0, 0)

        // An on-release action must not also fire on press.
        primary.calls.size shouldBe 0
    }

    test("actions run in registration order and the first consumer ends the chain") {
        val first = Recording(consumes = true)
        val second = Recording()
        val controller = controllerWith(
            InputRegistration(first, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(second, InputTrigger.UP, setOf(leftButton))
        )

        controller.touchUp(0, 0, 0, 0) shouldBe true

        first.calls.size shouldBe 1
        // Fails if dispatch runs every action instead of stopping, which would report the last one's result.
        second.calls.size shouldBe 0
    }

    test("a chain where nothing consumes reports not handled") {
        val primary = Recording()
        val controller = controllerWith(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        // The multiplexer reads this to decide whether processors behind the controller still see the event.
        controller.touchUp(0, 0, 0, 0) shouldBe false
        primary.calls.size shouldBe 1
    }

    test("a registered action that never overrides handle consumes nothing") {
        val controller = controllerWith(InputRegistration(silent, InputTrigger.UP, setOf(leftButton)))

        controller.touchUp(0, 0, 0, 0) shouldBe false
    }

    test("dragging runs the action whose code is held, not one that merely exists") {
        val dragPrimary = Recording()
        val dragSecondary = Recording()
        val controller = controllerWith(
            InputRegistration(dragPrimary, InputTrigger.DRAGGED, setOf(leftButton)),
            InputRegistration(dragSecondary, InputTrigger.DRAGGED, setOf(rightButton))
        )

        service.buttonDown(1)
        controller.touchDragged(5, 6, 0)

        // touchDragged carries no button, so held state is the only separator between the two drags.
        dragSecondary.calls.size shouldBe 1
        dragPrimary.calls.size shouldBe 0
        dragSecondary.calls.single().code shouldBe null
    }

    test("dragging with nothing held runs nothing") {
        val dragPrimary = Recording()
        val controller = controllerWith(InputRegistration(dragPrimary, InputTrigger.DRAGGED, setOf(leftButton)))

        controller.touchDragged(0, 0, 0) shouldBe false
        dragPrimary.calls.size shouldBe 0
    }

    test("keys dispatch by keycode on their own trigger") {
        val onKeyUp = Recording(consumes = true)
        val controller = controllerWith(InputRegistration(onKeyUp, InputTrigger.UP, setOf(InputCode.Key(62))))

        controller.keyDown(62) shouldBe false
        controller.keyUp(62) shouldBe true

        onKeyUp.calls.size shouldBe 1
    }

    test("a cancelled touch dispatches on its own trigger rather than as an up") {
        val onUp = Recording()
        val onCancel = Recording()
        val controller = controllerWith(
            InputRegistration(onUp, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(onCancel, InputTrigger.CANCELLED, setOf(leftButton))
        )

        controller.touchCancelled(0, 0, 0, 0)

        onCancel.calls.size shouldBe 1
        onUp.calls.size shouldBe 0
    }
})
