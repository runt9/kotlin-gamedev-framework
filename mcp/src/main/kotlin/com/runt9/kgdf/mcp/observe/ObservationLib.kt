package com.runt9.kgdf.mcp.observe

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

/**
 * A game's kinds are asked before the built-in ones, so a game kind wins a tie by being the more specific
 * answer.
 */
interface TargetKind {
    /** The name a client sees. */
    val label: String

    /** Whether the walk stops here, treating everything below this actor as part of one thing. */
    val stopDescent: Boolean get() = false

    fun matches(actor: Actor): Boolean

    /**
     * Called only for an actor this kind already [matches], which is what lets an implementation cast.
     *
     * The default reads whatever the control says, so a caller can pick a target by reading it. Override
     * wherever text is the wrong address, or is not there to read.
     */
    fun keyFor(actor: Actor): String = actor.name ?: textOf(actor) ?: label
}

/**
 * Always registered and always last, so a game cannot lose button detection by forgetting to include them.
 *
 * **Declaration order is precedence**, since the walk takes the first match.
 */
enum class FrameworkTargetKind(override val label: String) : TargetKind {
    /** Before [BUTTON], which `CheckBox` extends. */
    CHECKBOX("checkbox") {
        override fun matches(actor: Actor) = actor is CheckBox
    },

    SELECT_BOX("selectBox") {
        override fun matches(actor: Actor) = actor is SelectBox<*>
    },

    /** Carries only an `InputListener`, so [CLICKABLE] would never claim it. */
    SLIDER("slider") {
        override fun matches(actor: Actor) = actor is Slider
    },

    BUTTON("button") {
        override fun matches(actor: Actor) = actor is Button
    },

    /** Last, so it only picks up what nothing above recognized. */
    CLICKABLE("clickable") {
        override fun matches(actor: Actor) = actor.listeners.any { it is ClickListener }
    }
}

/**
 * **Do not address anything by this whose children render what the player cannot see.** A face-down card still
 * builds a `Label` holding its real value, so a key derived from text would publish it.
 */
fun textOf(actor: Actor): String? = when (actor) {
    is Label -> actor.text.toString().ifBlank { null }
    is TextButton -> actor.text.toString().ifBlank { null }
    is SelectBox<*> -> actor.selected?.toString()?.ifBlank { null }
    is Group -> actor.children.firstNotNullOfOrNull { textOf(it) }
    else -> null
}

/**
 * Positions are **screen** coordinates, ready to hand straight back as a click.
 *
 * Keys are unique within one observation but not across observations, since they describe a layout rather than
 * an identity. A greyed-out control is still here with [enabled] false, because dropping it would turn a click
 * on a disabled button into "no such target".
 */
data class Target(
    val key: String,
    val kind: TargetKind,
    val screenX: Int,
    val screenY: Int,
    val enabled: Boolean,
    val reachable: Boolean,
    val blockedBy: String?
)
