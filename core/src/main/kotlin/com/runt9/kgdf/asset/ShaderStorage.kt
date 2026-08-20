package com.runt9.kgdf.asset

import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.runt9.kgdf.log.kgdfLogger

class ShaderStorage {
    private val logger = kgdfLogger()
    private val storedShaders = mutableMapOf<ShaderDefinition, ShaderProgram>()

    fun compileAndRegisterShader(definition: ShaderDefinition) {
        val shader = ShaderProgram(definition.shaderVert, definition.shaderFragment)
        if (!shader.isCompiled) {
            logger.error { "Shader $definition did not compile" }
            throw ShaderException(definition, "Shader did not compile")
        } else {
            logger.info { "Shader compiled $definition" }
            storedShaders[definition] = shader
        }
    }

    fun getShader(definition: ShaderDefinition): ShaderProgram {
        if (!storedShaders.containsKey(definition)) {
            throw ShaderException(definition, "No shader defined for $definition")
        }

        return storedShaders[definition]!!
    }
}

class ShaderException(val definition: ShaderDefinition, message: String) : RuntimeException(message)
