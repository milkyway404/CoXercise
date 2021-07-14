package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

const val MAX_LIFE = 100f
const val MAX_SHIElD = 100f

class PlayerComponent : Component, Pool.Poolable {

    var life = MAX_LIFE
    var maxLife = MAX_LIFE
    var shield = 0f
    var maxShield = MAX_SHIElD
    var distance = 0f

    override fun reset() {
         life = MAX_LIFE
         maxLife = MAX_LIFE
         shield = 0f
         maxShield = MAX_SHIElD
         distance = 0f
    }

    companion object{
        val mapper = mapperFor<PlayerComponent>()
    }
}