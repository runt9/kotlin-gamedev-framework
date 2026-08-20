package com.runt9.kgdf.asset

interface ShaderDefinition {
    val shaderVert: String
    val shaderFragment: String
}

object PixelArtShaderDefinition : ShaderDefinition {
    override val shaderVert = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoords;
        void main()
        {
            v_color = a_color;
            v_color.a = v_color.a * (255.0/254.0);
            v_texCoords = a_texCoord0;
            gl_Position =  u_projTrans * a_position;
        }
    """.trimIndent()

    override val shaderFragment = """
        #ifdef GL_ES
        #extension GL_OES_standard_derivatives : enable
        #define LOWP lowp
        precision mediump float;
        #else
        #define LOWP
        #endif
        varying vec2 v_texCoords;
        varying LOWP vec4 v_color;
        uniform sampler2D u_texture;
        uniform vec2 u_textureResolution;
        void main() {
            vec2 uv = v_texCoords * u_textureResolution;
            vec2 seam = floor(uv+.5);
            uv = seam + clamp((uv-seam)/distance(dFdx(uv),dFdy(uv)), -.5, .5);
            gl_FragColor = texture2D(u_texture, uv/u_textureResolution) * v_color;
        }
    """.trimIndent()
}
