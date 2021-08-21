package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

const val MAX_HP = 100
const val MAX_MP = 100

open class PlayerComponent : Component, Pool.Poolable {

    open var hp = MAX_HP
    open val maxHp = MAX_HP
    open var mp = 0
    open val maxMp = MAX_MP
    var distance = 0f
    var isAttacking = false

    override fun reset() {
         hp = MAX_HP
         mp = 0
         distance = 0f
        isAttacking = false
    }

    companion object{
        val mapper = mapperFor<PlayerComponent>()
    }
}