package com.p4pProject.gameTutorial.ecs.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas

enum class TextureAsset(
    fileName:String,
    directory:String = "graphics",
    val descriptor : AssetDescriptor<Texture> = AssetDescriptor("$directory/$fileName", Texture::class.java)
){
    // Simple picture assets
    BACKGROUND("background.png")
}

enum class TextureAtlasAsset(
    fileName:String,
    directory:String = "graphics",
    val descriptor : AssetDescriptor<TextureAtlas> = AssetDescriptor("$directory/$fileName", TextureAtlas::class.java)
){
    // Animated assets
    GAME_GRAPHICS("graphics.atlas")
}