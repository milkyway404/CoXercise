package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

const val BOSS_MAX_HP = 300
//const val MAX_SHIElD = 100f

class BossComponent : Component, Pool.Poolable {

    var hp = BOSS_MAX_HP
    var maxHp = BOSS_MAX_HP
    var offset = 1.5625f
    //var shield = 0f
    //var maxShield = MAX_SHIElD
    var distance = 0f
    var isAttacking = false
    var isStunned = false
    var isHurt = false

    override fun reset() {
        hp = BOSS_MAX_HP
        maxHp = BOSS_MAX_HP
        //shield = 0f
        //maxShield = MAX_SHIElD
        distance = 0f
        isAttacking = false
        isStunned = false
        isHurt = false
    }

    companion object{
        val mapper = mapperFor<BossComponent>()
    }
}