package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.mapperFor

const val WARRIOR_MAX_HP = 200
const val WARRIOR_MAX_MP = 50
const val WARRIOR_SPECIAL_ATTACK_MP_COST = 50
const val WARRIOR_NORMAL_ATTACK_DAMAGE = 5
const val WARRIOR_SPECIAL_ATTACK_DAMAGE = 20

const val ARCHER_MAX_HP = 50
const val ARCHER_MAX_MP = 50
const val ARCHER_SPECIAL_ATTACK_MP_COST = 50
const val ARCHER_NORMAL_ATTACK_DAMAGE = 10
const val ARCHER_SPECIAL_ATTACK_DAMAGE = 50

const val PRIEST_MAX_HP = 50
const val PRIEST_MAX_MP = 200
const val PRIEST_SPECIAL_ATTACK_MP_COST = 100
const val PRIEST_NORMAL_ATTACK_DAMAGE = 5
const val PRIEST_SPECIAL_ATTACK_HEAL = 30

class PlayerComponent : Component, Pool.Poolable {

    var hp = WARRIOR_MAX_HP
    var maxHp = WARRIOR_MAX_HP
    var mp = 0
    var maxMp = WARRIOR_MAX_MP
    var specialAttackMpCost = 0
    var distance = 0f
    var isAttacking = false
    var isSpecialAttacking = false
    var characterType = CharacterType.WARRIOR
    var normalAttackDamage = 0
    var specialAttackDamageOrHeal = 0

    fun setAsWarrior(){
        characterType = CharacterType.WARRIOR
        hp = WARRIOR_MAX_HP
        mp = WARRIOR_MAX_MP
        maxHp = WARRIOR_MAX_HP
        maxMp = WARRIOR_MAX_MP
        specialAttackMpCost = WARRIOR_SPECIAL_ATTACK_MP_COST
        normalAttackDamage = WARRIOR_NORMAL_ATTACK_DAMAGE
        specialAttackDamageOrHeal = WARRIOR_SPECIAL_ATTACK_DAMAGE
    }

    fun setAsArcher(){
        characterType = CharacterType.ARCHER
        hp = ARCHER_MAX_HP
        mp = ARCHER_MAX_HP
        maxHp = ARCHER_MAX_HP
        maxMp = ARCHER_MAX_MP
        specialAttackMpCost = ARCHER_SPECIAL_ATTACK_MP_COST
        normalAttackDamage = ARCHER_NORMAL_ATTACK_DAMAGE
        specialAttackDamageOrHeal = ARCHER_SPECIAL_ATTACK_DAMAGE
    }

    fun setAsPriest(){
        characterType = CharacterType.PRIEST
        hp = PRIEST_MAX_HP
        mp = PRIEST_MAX_MP
        maxHp = PRIEST_MAX_HP
        maxMp = PRIEST_MAX_MP
        specialAttackMpCost = PRIEST_SPECIAL_ATTACK_MP_COST
        normalAttackDamage = PRIEST_NORMAL_ATTACK_DAMAGE
        specialAttackDamageOrHeal = PRIEST_SPECIAL_ATTACK_HEAL
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