package com.runt9.kgdf.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** `KtxInputAdapter` is a plain interface, so the shim needs no LibGDX application to exercise. */
class InputTrackerTest : FunSpec({
    val primary = object : InputAction {}

    fun tracker(): Pair<InputTracker, InputTrackingService> {
        val service = InputTrackingService()
        service.registerActions(
            listOf(InputRegistration(primary, InputTrigger.UP, setOf(InputCode.Button(0), InputCode.Key(62))))
        )
        return InputTracker(service) to service
    }

    test("every handler returns false so nothing downstream is starved") {
        val (tracker, _) = tracker()

        // Returning true from any of these silently stops the stage behind it from seeing that input, and no
        // other test would notice.
        tracker.keyDown(62) shouldBe false
        tracker.keyUp(62) shouldBe false
        tracker.touchDown(0, 0, 0, 0) shouldBe false
        tracker.touchUp(0, 0, 0, 0) shouldBe false
        tracker.touchCancelled(0, 0, 0, 0) shouldBe false
    }

    test("key and touch events reach the service") {
        val (tracker, service) = tracker()

        tracker.keyDown(62)
        service.isHeld(primary) shouldBe true
        tracker.keyUp(62)
        service.isHeld(primary) shouldBe false

        tracker.touchDown(0, 0, 0, 0)
        service.anyButtonDown shouldBe true
        tracker.touchUp(0, 0, 0, 0)
        service.anyButtonDown shouldBe false
    }

    test("a cancelled touch releases like a normal one") {
        val (tracker, service) = tracker()
        tracker.touchDown(0, 0, 0, 0)

        tracker.touchCancelled(0, 0, 0, 0)

        // Without this the button stays held forever, the same stuck-drag the focus hook guards.
        service.anyButtonDown shouldBe false
        service.isHeld(primary) shouldBe false
    }
})
