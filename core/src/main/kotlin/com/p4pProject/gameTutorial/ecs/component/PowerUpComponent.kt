package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.ecs.asset.SoundAsset
import ktx.ashley.mapperFor

enum class PowerUpType (
    val animationType: AnimationType,
    val mpGain: Int = 20,
    val soundAsset: SoundAsset
){
    NONE(AnimationType.NONE, soundAsset = SoundAsset.BLOCK),
    MP_GAIN(AnimationType.MP, soundAsset = SoundAsset.SHIELD) // TODO change to name mp
}

class PowerUpComponent : Component, Pool.Poolable{

    var type = PowerUpType.NONE
    var duration = 0F

    override fun reset() {
        type = PowerUpType.NONE
    }

    companion object {
        val mapper = mapperFor<PowerUpComponent>()
    }
}