package com.runt9.kgdf.mcp.observe

import com.badlogic.gdx.Gdx
import com.runt9.kgdf.game.KgdfGame
import com.runt9.kgdf.ui.core.BaseScreen
import com.runt9.kgdf.ui.core.BasicStage

/**
 * The stages currently taking input. Dialogs need no separate lookup: `DialogManager` shows them on the
 * screen's own stage.
 *
 * Empty means no screen is showing, which is a result to report rather than a failure.
 */
fun shownScreenStages(): List<BasicStage> {
    val game = Gdx.app?.applicationListener as? KgdfGame ?: return emptyList()
    return (game.shownScreen as? BaseScreen)?.stages ?: emptyList()
}
