package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

enum class PowerUpType (
    val animationType: AnimationType,
    val lifeGain:Float = 0f,
    val shieldGain:Float = 0f,
    val speedGain:Float = 0f
){
    NONE(AnimationType.NONE),
    SPEED_1(AnimationType.SPEED_1, speedGain = 3f),
    SPEED_2(AnimationType.SPEED_2, speedGain = 3.75f),
    LIFE(AnimationType.LIFE, lifeGain = 25f),
    SHIELD(AnimationType.SHIELD, shieldGain = 25f)
}


class PowerUpComponent : Component, Pool.Poolable{

    var type = PowerUpType.NONE

    override fun reset() {
        type = PowerUpType.NONE
    }

    companion object {
        val mapper = mapperFor<PowerUpComponent>()
    }
}