package com.runt9.kgdf.ui.core

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.kotcrab.vis.ui.widget.VisImage
import com.kotcrab.vis.ui.widget.color.internal.ShaderImage
import com.runt9.kgdf.asset.PixelArtShaderDefinition
import com.runt9.kgdf.asset.ShaderStorage
import com.runt9.kgdf.ext.inject
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class TextureFilteringShaderImage(
    shader: ShaderProgram,
    private val texture: Texture,
    private val minFilter: Texture.TextureFilter,
    private val magFilter: Texture.TextureFilter,
    private val setUniforms: ShaderProgram.() -> Unit
) : ShaderImage(shader, texture) {
    private val currentMin = texture.minFilter
    private val currentMag = texture.magFilter

    override fun draw(batch: Batch?, parentAlpha: Float) {
        texture.setFilter(minFilter, magFilter)
        super.draw(batch, parentAlpha)
        texture.setFilter(currentMin, currentMag)
    }

    override fun setShaderUniforms(shader: ShaderProgram) {
        shader.setUniforms()
    }
}

@Scene2dDsl
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.textureFilteringShaderImage(
    shader: ShaderProgram,
    texture: Texture,
    minFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
    magFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
    noinline setUniforms: ShaderProgram.() -> Unit = {},
    init: (@Scene2dDsl TextureFilteringShaderImage).(S) -> Unit = {},
): VisImage {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return actor(TextureFilteringShaderImage(shader, texture, minFilter, magFilter, setUniforms), init)
}


@Scene2dDsl
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.pixelArtShaderImage(
    texture: Texture,
    width: Float = 16f,
    height: Float = 16f,
    minFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
    magFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
    init: (@Scene2dDsl TextureFilteringShaderImage).(S) -> Unit = {},
): VisImage {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    val shader = inject<ShaderStorage>().getShader(PixelArtShaderDefinition)
    val shaderImage = TextureFilteringShaderImage(shader, texture, minFilter, magFilter) {
        setUniformf("u_textureResolution", width, height)
    }

    return actor(shaderImage, init)
}
