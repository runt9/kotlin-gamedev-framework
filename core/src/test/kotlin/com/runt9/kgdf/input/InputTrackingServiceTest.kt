package com.runt9.kgdf.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * No LibGDX application and no scheduler. If these ever need a Gdx fixture, something engine-shaped has leaked
 * into the service.
 */
class InputTrackingServiceTest : FunSpec({
    // Mirrors the real collision: libGDX numbers both keycodes and mouse buttons from 0.
    val leftButton = InputCode.Button(0)
    val rightButton = InputCode.Button(1)
    val keyZero = InputCode.Key(0)
    val modifierKey = InputCode.Key(129)

    val primary = object : InputAction {}
    val secondary = object : InputAction {}

    fun service(vararg registrations: InputRegistration) =
        InputTrackingService().apply { registerActions(registrations.toList()) }

    test("an action reports held between its press and its release") {
        val service = service(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        service.isHeld(primary) shouldBe false

        service.buttonDown(0)
        service.isHeld(primary) shouldBe true

        service.buttonUp(0)
        service.isHeld(primary) shouldBe false
    }

    test("a key and a button sharing a numeric code do not alias") {
        val service = service(
            InputRegistration(primary, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(secondary, InputTrigger.UP, setOf(keyZero))
        )

        service.keyDown(0)

        // Fails if keys and buttons are ever collapsed into one Set<Int>, which reads as correct until a game
        // binds key 0 or a mouse button, then reports the wrong thing with no error.
        service.isHeld(secondary) shouldBe true
        service.isHeld(primary) shouldBe false

        service.keyUp(0)
        service.buttonDown(0)

        service.isHeld(primary) shouldBe true
        service.isHeld(secondary) shouldBe false
    }

    test("an action bound to a key and a button is held when either is down") {
        val service = service(InputRegistration(primary, InputTrigger.UP, setOf(leftButton, modifierKey)))

        service.keyDown(129)
        service.isHeld(primary) shouldBe true

        service.keyUp(129)
        service.isHeld(primary) shouldBe false

        service.buttonDown(0)
        service.isHeld(primary) shouldBe true
    }

    test("a held modifier does not count as a button being down") {
        val service = service(InputRegistration(secondary, InputTrigger.UP, setOf(modifierKey)))

        // Drag handling turns on this difference. Deriving anyButtonDown from held actions would keep a drag
        // alive on a held modifier key.
        service.keyDown(129)
        service.isHeld(secondary) shouldBe true
        service.anyButtonDown shouldBe false

        service.buttonDown(0)
        service.anyButtonDown shouldBe true
    }

    test("actionsFor matches on trigger as well as code") {
        val service = service(
            InputRegistration(primary, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(secondary, InputTrigger.DOWN, setOf(leftButton))
        )

        // Same code, different triggers. Matching on code alone would fire an on-release action on press too.
        service.actionsFor(InputTrigger.UP, leftButton) shouldBe listOf(primary)
        service.actionsFor(InputTrigger.DOWN, leftButton) shouldBe listOf(secondary)
        service.actionsFor(InputTrigger.CANCELLED, leftButton) shouldBe emptyList()
    }

    test("actionsFor ignores a different code on the same trigger") {
        val service = service(
            InputRegistration(primary, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(secondary, InputTrigger.UP, setOf(rightButton))
        )

        // Both are UP actions, so only the arriving button separates them.
        service.actionsFor(InputTrigger.UP, rightButton) shouldBe listOf(secondary)
    }

    test("actionsFor preserves registration order as precedence") {
        val service = service(
            InputRegistration(secondary, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(primary, InputTrigger.UP, setOf(leftButton))
        )

        // Order is the only way to say which of two actions on one code wins, so it must survive the lookup.
        service.actionsFor(InputTrigger.UP, leftButton) shouldBe listOf(secondary, primary)
    }

    test("heldActionsFor selects by held state rather than an arriving code") {
        val service = service(
            InputRegistration(primary, InputTrigger.DRAGGED, setOf(leftButton)),
            InputRegistration(secondary, InputTrigger.DRAGGED, setOf(rightButton))
        )

        service.heldActionsFor(InputTrigger.DRAGGED) shouldBe emptyList()

        service.buttonDown(1)

        service.heldActionsFor(InputTrigger.DRAGGED) shouldBe listOf(secondary)
    }

    test("heldActionsFor ignores held codes registered for a different trigger") {
        val service = service(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        service.buttonDown(0)

        // Fails if the trigger check is dropped, which would make every held button fire drag actions.
        service.heldActionsFor(InputTrigger.DRAGGED) shouldBe emptyList()
    }

    test("an unregistered action never reports held") {
        val service = service(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        service.buttonDown(0)

        service.isHeld(secondary) shouldBe false
    }

    test("codes with no action are still tracked") {
        val service = service(InputRegistration(primary, InputTrigger.UP, setOf(leftButton)))

        service.keyDown(999)

        // Registering it later needs no replay, because only resolution reads the list.
        service.isHeld(InputCode.Key(999)) shouldBe true
        service.registerActions(listOf(InputRegistration(secondary, InputTrigger.UP, setOf(InputCode.Key(999)))))
        service.isHeld(secondary) shouldBe true
    }

    test("clear drops keys and buttons together") {
        val service = service(
            InputRegistration(primary, InputTrigger.UP, setOf(leftButton)),
            InputRegistration(secondary, InputTrigger.UP, setOf(modifierKey))
        )
        service.keyDown(129)
        service.buttonDown(0)

        service.clear()

        service.isHeld(primary) shouldBe false
        service.isHeld(secondary) shouldBe false
        service.anyButtonDown shouldBe false
    }
})
