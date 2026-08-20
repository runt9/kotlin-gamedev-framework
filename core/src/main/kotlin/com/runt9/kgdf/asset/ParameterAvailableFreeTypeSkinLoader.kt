package com.runt9.kgdf.asset

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.g2d.TextureAtlas

class ParameterAvailableFreeTypeSkinLoader(resolver: FileHandleResolver) : SkinLoader(resolver) {
    override fun newSkin(atlas: TextureAtlas) = ParameterAvailableFreeTypeSkin(atlas)
}
