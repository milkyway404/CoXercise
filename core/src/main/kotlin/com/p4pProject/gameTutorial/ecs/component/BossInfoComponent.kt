package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class BossInfoComponent: Component, Pool.Poolable {
    lateinit var boss: Entity

    override fun reset() { }

    fun setBossInfo(boss: Entity) {
        this.boss = boss
    }

    fun bossIsInitialized(): Boolean {
        return (::boss.isInitialized)
    }

    companion object{
        val mapper = mapperFor<BossInfoComponent>()
    }
}