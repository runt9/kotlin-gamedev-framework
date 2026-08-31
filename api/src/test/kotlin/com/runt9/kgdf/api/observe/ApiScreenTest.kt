package com.runt9.kgdf.api.observe

import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.view.View
import com.runt9.kgdf.ui.viewModel.ViewModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KClass

/**
 * [ApiScreen.forController] matches the declared class exactly while [ApiScreen.of] matches an instance by
 * `isInstance`. A subclass of a declared controller is the only input that tells the two predicates apart, so
 * without the subclass cases here `forController` could be rewritten to use `isInstance` and nothing would fail.
 */
class ApiScreenTest : FunSpec({
    // The registry is process-global with no teardown, so a leaked entry would decide a later case.
    beforeTest {
        ApiScreen.screens.clear()
        ApiScreen.registerScreens(listOf(TestScreen.PRIMARY))
    }

    test("forController resolves the class its screen declares") {
        ApiScreen.forController(PrimaryController::class) shouldBe TestScreen.PRIMARY
    }

    test("forController refuses a subclass of the declared class") {
        shouldThrow<IllegalStateException> { ApiScreen.forController(SubclassedController::class) }
    }

    test("of resolves an instance of that same subclass") {
        ApiScreen.of(SubclassedController()) shouldBe TestScreen.PRIMARY
    }

    test("of answers UNKNOWN for a controller no screen declares") {
        ApiScreen.of(UnlistedController()) shouldBe DefaultApiScreen.UNKNOWN
    }

    test("of answers NONE when nothing is showing") {
        ApiScreen.of(null) shouldBe DefaultApiScreen.NONE
    }
})

private enum class TestScreen(override val route: String, override val controller: KClass<out Controller>?) : ApiScreen {
    PRIMARY("primary", PrimaryController::class)
}

private abstract class StubController : Controller {
    // Both predicates match on type alone, so a controller here never needs a renderable pair.
    override val vm: ViewModel get() = error("A stub controller has no ViewModel")
    override val view: View get() = error("A stub controller has no View")
}

private open class PrimaryController : StubController()

private class SubclassedController : PrimaryController()

/** A sibling of [PrimaryController], not a subclass: `isInstance` must miss it for the UNKNOWN case to mean anything. */
private class UnlistedController : StubController()
