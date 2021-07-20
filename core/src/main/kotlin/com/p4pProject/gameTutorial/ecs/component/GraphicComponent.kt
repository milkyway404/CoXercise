package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.UNIT_SCALE
import ktx.ashley.mapperFor


class GraphicComponent: Component, Pool.Poolable {
    val sprite = Sprite()
    var isBackground = false

    override fun reset() {
        sprite.texture = null
        sprite.setColor(1f,1f,1f,1f)
    }

    fun setSpriteRegion(region: TextureRegion){
        sprite.run {
            setRegion(region)
            setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
            setOriginCenter()
        }
    }

    fun isBackground(){
        isBackground = true
    }

    companion object{
        val mapper = mapperFor<GraphicComponent>()
    }
}