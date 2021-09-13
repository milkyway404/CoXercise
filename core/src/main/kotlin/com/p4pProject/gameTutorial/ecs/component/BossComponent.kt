package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

const val BOSS_MAX_HP = 300

class BossComponent : Component, Pool.Poolable {

    var hp = BOSS_MAX_HP
    var maxHp = BOSS_MAX_HP

    var offset = 1.5625f
    var distance = 0f
    var isAttackReady = true
    var isAttacking = false
    var isStunned = false
    var isHurt = false
    var attackDamage = 0

    override fun reset() {
        hp = BOSS_MAX_HP
        maxHp = BOSS_MAX_HP
        distance = 0f
        isAttacking = false
        isStunned = false
        isHurt = false
    }

    companion object{
        val mapper = mapperFor<BossComponent>()
    }
}