package com.runt9.kgdf.api.observe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.game.KgdfGame
import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.core.UiScreen
import com.runt9.kgdf.ui.view.DialogView
import kotlinx.coroutines.withTimeout
import ktx.async.onRenderingThread
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

/** Generous: a timeout here means the render loop stopped, which is worth surfacing rather than waiting out. */
internal val RENDER_TIMEOUT = 10.seconds

/**
 * One hop onto the rendering thread, bounded. Everything the harness reads or drives goes through here, so an
 * answer cannot describe two different frames and a stalled render loop surfaces rather than hanging.
 *
 * Endpoints should use `ApiController.onRender` or `ScreenApiController.onScreen`. Never call this from inside
 * another hop: it posts and suspends, so the nested call waits on a frame the outer block is holding.
 */
suspend fun <T> renderHop(block: () -> T): T = withTimeout(RENDER_TIMEOUT) { onRenderingThread { block() } }

/**
 * Which screen the player is on. **Screen here means screen or topmost open dialog**, which is what a player
 * means by it too.
 */
object ShownScreen {
    private val multiplexer by lazyInject<InputMultiplexer>()

    /**
     * The controller for the topmost open dialog if there is one, otherwise for the screen behind it. This is
     * both what a game keys its observers on and the instance they read.
     *
     * The stage comes from the multiplexer because that is the list dispatch itself walks, so nothing is live
     * here without being live for a real click too. **Last wins** -- later processors sit on top.
     *
     * The controller is pushed down into observers rather than injected by them because
     * [com.runt9.kgdf.ui.DialogManager] builds a dialog's controller with `dynamicInject`, which constructs
     * rather than resolving from the DI container.
     */
    fun shown(): Controller? {
        val stage = multiplexer.processors.filterIsInstance<Stage>().lastOrNull() ?: return null
        stage.root.children.filterIsInstance<DialogView>().lastOrNull { it.isVisible }?.let { return it.controller }

        val game = Gdx.app?.applicationListener as? KgdfGame ?: return null
        return (game.shownScreen as? UiScreen)?.uiController
    }
}
