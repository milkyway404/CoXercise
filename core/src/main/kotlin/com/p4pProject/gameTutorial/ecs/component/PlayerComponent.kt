package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.mapperFor

const val WARRIOR_MAX_HP = 200
const val WARRIOR_MAX_MP = 50

const val ARCHER_MAX_HP = 50
const val ARCHER_MAX_MP = 50

const val PRIEST_MAX_HP = 50
const val PRIEST_MAX_MP = 200

class PlayerComponent : Component, Pool.Poolable {

    var hp = WARRIOR_MAX_HP
    var maxHp = WARRIOR_MAX_HP
    var mp = 0
    var maxMp = WARRIOR_MAX_MP
    var distance = 0f
    var isAttacking = false
    var isSpecialAttacking = false
    var characterType = CharacterType.WARRIOR


    fun setAsWarrior(){
        characterType = CharacterType.WARRIOR
        hp = WARRIOR_MAX_HP
        mp = WARRIOR_MAX_MP
        maxHp = WARRIOR_MAX_HP
        maxMp = WARRIOR_MAX_MP
    }

    fun setAsArcher(){
        characterType = CharacterType.ARCHER
        hp = ARCHER_MAX_HP
        mp = ARCHER_MAX_HP
        maxHp = ARCHER_MAX_HP
        maxMp = ARCHER_MAX_MP
    }

    fun setAsPriest(){
        characterType = CharacterType.PRIEST
        hp = PRIEST_MAX_HP
        mp = PRIEST_MAX_MP
        maxHp = PRIEST_MAX_HP
        maxMp = PRIEST_MAX_MP
    }


    override fun reset() {
        hp = when(characterType){
            CharacterType.WARRIOR -> {
                WARRIOR_MAX_HP
            }
            CharacterType.ARCHER -> {
                ARCHER_MAX_HP
            }
            CharacterType.PRIEST -> {
                PRIEST_MAX_HP
            }
        }
        mp = 0
         distance = 0f
        isAttacking = false
    }

    companion object{
        val mapper = mapperFor<PlayerComponent>()
    }
}