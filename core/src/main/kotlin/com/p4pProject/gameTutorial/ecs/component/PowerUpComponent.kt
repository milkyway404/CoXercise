package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.ecs.asset.SoundAsset
import ktx.ashley.mapperFor

enum class PowerUpType (
    val animationType: AnimationType,
    val lifeGain: Int = 0,
    val shieldGain:Float = 0f,
    val speedGain:Float = 0f,
    val soundAsset: SoundAsset
){
    NONE(AnimationType.NONE, soundAsset = SoundAsset.BLOCK),
    SPEED_1(AnimationType.SPEED_1, speedGain = 0f, soundAsset = SoundAsset.BOOST_1),
    SPEED_2(AnimationType.SPEED_2, speedGain = 0f, soundAsset = SoundAsset.BOOST_2),
    LIFE(AnimationType.LIFE, lifeGain = 25, soundAsset = SoundAsset.LIFE),
    SHIELD(AnimationType.SHIELD, shieldGain = 25f, soundAsset = SoundAsset.SHIELD)
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