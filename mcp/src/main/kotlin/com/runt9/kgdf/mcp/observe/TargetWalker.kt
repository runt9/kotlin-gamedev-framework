package com.runt9.kgdf.mcp.observe

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.utils.Disableable
import kotlin.math.roundToInt

/**
 * **Call this on the rendering thread.** It reads live actor geometry and hit-tests against the same stage the
 * renderer is walking, so a read from anywhere else races layout.
 *
 * @param gameKinds what only this game can recognize, asked before the built-in kinds
 */
class TargetWalker(gameKinds: List<TargetKind>) {
    private val kinds = gameKinds + FrameworkTargetKind.entries

    /** An actor with no stage has nothing addressable under it: there is no space to report a click in. */
    fun targetsUnder(root: Actor): List<Target> {
        val stage = root.stage ?: return emptyList()
        val targets = mutableListOf<Target>()
        walk(stage, root, targets)
        return targets.withUniqueKeys()
    }

    private fun walk(stage: Stage, actor: Actor, into: MutableList<Target>) {
        if (!actor.isVisible) return

        val kind = kinds.firstOrNull { it.matches(actor) }
        if (kind != null) {
            into += target(stage, actor, kind)
            if (kind.stopDescent) return
        }

        // Children, never a Table's cells: dialogs are added with addActor and hold no cell, so a cell-based
        // descent finds no dialog at all while still looking like it walked the tree.
        if (actor is Group) actor.children.forEach { walk(stage, it, into) }
    }

    private fun target(stage: Stage, actor: Actor, kind: TargetKind): Target {
        val center = actor.localToStageCoordinates(Vector2(actor.width / 2f, actor.height / 2f))
        val hit = stage.hit(center.x, center.y, true)

        // A childrenOnly parent is never itself the hit result even when clicking it works.
        val reachable = hit != null && (hit === actor || hit.isDescendantOf(actor))

        // Not viewport.project, which omits the Y flip and mirrors every reported position.
        val screen = stage.stageToScreenCoordinates(Vector2(center))

        return Target(
            key = kind.keyFor(actor),
            kind = kind,
            screenX = screen.x.roundToInt(),
            screenY = screen.y.roundToInt(),
            enabled = actor.isEnabled,
            reachable = reachable,
            blockedBy = if (reachable) null else hit?.let { it.name ?: it.javaClass.simpleName }
        )
    }
}

/** Two "Buy" buttons are two targets, so the second gets a suffix rather than becoming unaddressable. */
private fun List<Target>.withUniqueKeys(): List<Target> {
    val seen = mutableMapOf<String, Int>()

    return map { target ->
        val count = seen.merge(target.key, 1, Int::plus)!!
        if (count == 1) target else target.copy(key = "${target.key}#$count")
    }
}

/** Rejects rather than accepts: `Table` and `Group` default to `childrenOnly`, not `enabled`. */
private val Actor.isEnabled
    get() = touchable != Touchable.disabled && (this as? Disableable)?.isDisabled != true
