package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class FacingComponent : Component, Pool.Poolable{

    var direction = FacingDirection.NORTH
    var lastDirection = FacingDirection.NORTH

    override fun reset() {
        direction = FacingDirection.NORTH
    }

    companion object {
        val mapper = mapperFor<FacingComponent>()
    }
}

enum class FacingDirection {
    NORTH, SOUTH, EAST, WEST
}