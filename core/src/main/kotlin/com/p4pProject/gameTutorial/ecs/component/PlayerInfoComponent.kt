package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.get
import ktx.ashley.mapperFor

class PlayerInfoComponent: Component, Pool.Poolable {

    private lateinit var warrior: Entity
    private lateinit var archer: Entity
    private lateinit var priest: Entity

    override fun reset() {
        TODO("Not yet implemented")
    }

    fun setPlayerInfo(warrior: Entity, archer: Entity, priest: Entity) {
        this.warrior = warrior
        this.archer = archer
        this.priest = priest
    }

    fun getLowestHpCharacter(): Entity {
        val warriorHp = when (warrior[PlayerComponent.mapper]) {
            null -> 99999
            else -> warrior[PlayerComponent.mapper]!!.hp
        }
        val archerHp = when (archer[PlayerComponent.mapper]) {
            null -> 99999
            else -> archer[PlayerComponent.mapper]!!.hp
        }
        val priestHp = when (priest[PlayerComponent.mapper]) {
            null -> 99999
            else -> priest[PlayerComponent.mapper]!!.hp
        }

        return when {
            warriorHp <= archerHp && warriorHp <= priestHp -> warrior
            archerHp <= warriorHp && archerHp <= priestHp -> archer
            else -> priest
        }
    }

    fun getClosestCharacter(bossPosition: Vector3): Entity {
        val warriorDistance = when (warrior[TransformComponent.mapper]) {
            null -> 99999f
            else -> calculateDistance(warrior[TransformComponent.mapper]!!.position, bossPosition)
        }
        val archerDistance = when (archer[TransformComponent.mapper]) {
            null -> 99999f
            else -> calculateDistance(archer[TransformComponent.mapper]!!.position, bossPosition)
        }
        val priestDistance = when (priest[TransformComponent.mapper]) {
            null -> 99999f
            else -> calculateDistance(priest[TransformComponent.mapper]!!.position, bossPosition)
        }

        return when {
            warriorDistance <= archerDistance && warriorDistance <= priestDistance -> warrior
            archerDistance <= warriorDistance && archerDistance <= priestDistance -> archer
            else -> priest
        }
    }

    fun printPlayerHps() {
        if (warrior[PlayerComponent.mapper] == null) {
            Gdx.app.log("Warrior", "Dead")
        } else {
            Gdx.app.log("Warrior HP", warrior[PlayerComponent.mapper]!!.hp.toString())
        }

        if (archer[PlayerComponent.mapper] == null) {
            Gdx.app.log("Archer", "Dead")
        } else {
            Gdx.app.log("Archer HP", archer[PlayerComponent.mapper]!!.hp.toString())
        }

        if (priest[PlayerComponent.mapper] == null) {
            Gdx.app.log("Priest", "Dead")
        } else {
            Gdx.app.log("Priest HP", priest[PlayerComponent.mapper]!!.hp.toString())
        }
    }

    private fun calculateDistance(position1: Vector3, position2: Vector3): Float {
        return position1.dst(position2)
    }

    companion object{
        val mapper = mapperFor<PlayerInfoComponent>()
    }


}