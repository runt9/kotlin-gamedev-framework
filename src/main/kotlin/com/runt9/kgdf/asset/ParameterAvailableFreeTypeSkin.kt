package com.runt9.kgdf.asset

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.Json.ReadOnlySerializer
import com.badlogic.gdx.utils.JsonValue

/**
 * Custom implementation of FreeTypeSkin that actually saves off the FreeTypeFontParameters for each font as it's generated
 * so it can be used by runtime font regeneration.
 */
class ParameterAvailableFreeTypeSkin(atlas: TextureAtlas) : Skin(atlas) {
    val fontParams = mutableMapOf<String, FreeTypeFontParameter>()

    override fun getJsonLoader(skinFile: FileHandle): Json {
        val json: Json = super.getJsonLoader(skinFile)
        val skin: Skin = this

        json.setSerializer(FreeTypeFontGenerator::class.java, object : ReadOnlySerializer<FreeTypeFontGenerator?>() {
            override fun read(
                json: Json,
                jsonData: JsonValue, type: Class<*>?
            ): FreeTypeFontGenerator? {
                val path = json.readValue("font", String::class.java, jsonData)
                jsonData.remove("font")

                val hinting = Hinting.valueOf(
                    json.readValue(
                        "hinting",
                        String::class.java, "AutoMedium", jsonData
                    )
                )
                jsonData.remove("hinting")

                val minFilter = TextureFilter.valueOf(
                    json.readValue("minFilter", String::class.java, "Nearest", jsonData)
                )
                jsonData.remove("minFilter")

                val magFilter = TextureFilter.valueOf(
                    json.readValue("magFilter", String::class.java, "Nearest", jsonData)
                )
                jsonData.remove("magFilter")

                val parameter = json.readValue(FreeTypeFontParameter::class.java, jsonData)
                parameter.hinting = hinting
                parameter.minFilter = minFilter
                parameter.magFilter = magFilter
                fontParams[jsonData.name] = parameter
                val generator = FreeTypeFontGenerator(skinFile.parent().child(path))
                FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM)
                val font = generator.generateFont(parameter)
                skin.add(jsonData.name, font)
                if (parameter.incremental) {
                    generator.dispose()
                    return null
                } else {
                    return generator
                }
            }
        })

        return json
    }
}
