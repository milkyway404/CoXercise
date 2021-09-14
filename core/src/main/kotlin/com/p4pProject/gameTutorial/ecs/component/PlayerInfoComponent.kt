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

    private var warriorDead = false
    private var archerDead = false
    private var priestDead = false

    override fun reset() {
        TODO("Not yet implemented")
    }

    fun setPlayerInfo(warrior: Entity, archer: Entity, priest: Entity) {
        this.warrior = warrior
        this.archer = archer
        this.priest = priest
    }

    fun getLowestHpCharacter(): Entity {
        checkDeath()

        val warriorHp = if (warriorDead) {
            Int.MAX_VALUE
        } else {
            warrior[PlayerComponent.mapper]!!.hp
        }

        val archerHp = if (archerDead) {
            Int.MAX_VALUE
        } else {
            archer[PlayerComponent.mapper]!!.hp
        }

        val priestHp = if (priestDead) {
            Int.MAX_VALUE
        } else {
            priest[PlayerComponent.mapper]!!.hp
        }

        return when {
            !warriorDead && warriorHp <= archerHp && warriorHp <= priestHp -> warrior
            !archerDead && archerHp <= warriorHp && archerHp <= priestHp -> archer
            else -> priest
        }
    }

    fun getClosestCharacter(bossPosition: Vector3): Entity {
        checkDeath()

        val warriorDistance = if (warriorDead) {
            Float.MAX_VALUE
        } else {
            calculateDistance(warrior[TransformComponent.mapper]?.position, bossPosition)
        }

        val archerDistance = if (archerDead) {
            Float.MAX_VALUE
        } else {
            calculateDistance(archer[TransformComponent.mapper]?.position, bossPosition)
        }

        val priestDistance = if (priestDead) {
            Float.MAX_VALUE
        } else {
            calculateDistance(priest[TransformComponent.mapper]?.position, bossPosition)
        }

        return when {
            !warriorDead && warriorDistance <= archerDistance && warriorDistance <= priestDistance -> {
                warrior
            }
            !archerDead && archerDistance <= warriorDistance && archerDistance <= priestDistance -> {
                archer
            }
            else -> {
                priest
            }
        }
    }

    private fun checkDeath() {
        if (warriorDead || warrior[PlayerComponent.mapper] == null || warrior[PlayerComponent.mapper]!!.hp <= 0) {
            warriorDead = true
        }

        if (archerDead || archer[PlayerComponent.mapper] == null || archer[PlayerComponent.mapper]!!.hp <= 0) {
            archerDead = true
        }

        if (priestDead || priest[PlayerComponent.mapper] == null || priest[PlayerComponent.mapper]!!.hp <= 0) {
            priestDead = true
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

    private fun calculateDistance(playerPosition: Vector3?, bossPosition: Vector3): Float {
        return playerPosition?.dst(bossPosition) ?: Float.MAX_VALUE
    }

    companion object{
        val mapper = mapperFor<PlayerInfoComponent>()
    }


}