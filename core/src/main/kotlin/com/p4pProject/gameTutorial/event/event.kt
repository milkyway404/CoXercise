package com.p4pProject.gameTutorial.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ObjectMap
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.PowerUpType
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.collections.GdxSet

import kotlin.reflect.KClass


sealed class GameEvent{
    object PlayerDeath : GameEvent (){
        lateinit var characterType: CharacterType

        override fun toString() = "PlayerDeath(characterType=$characterType)"
    }
    object CollectPowerUp : GameEvent(){
        lateinit var  player:Entity
        var type = PowerUpType.NONE

        override fun toString() = "CollectPowerUp(player = $player, type=$type)"
    }

    object PlayerHit : GameEvent(){
        lateinit var player : Entity
        var hp = 0
        var maxHp = 0

        override fun toString() = "PlayerHit(player = $player, hp=$hp, maxHp=$maxHp)"
    }

    object BossHit : GameEvent(){
        lateinit var boss : Entity
        var hp = 0
        var maxHp = 0
        var isStun = false

        override fun toString() = "PlayerHit(player = $boss, hp=$hp, maxHp=$maxHp)"
    }

    object BossHitFinished : GameEvent(){

    }

    object BossDead : GameEvent(){

    }

    object WarriorAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object WarriorAttackFinishEvent : GameEvent (){
    }

    object WarriorSpecialAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object WarriorSpecialAttackFinishEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }


    object ArcherAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object ArcherSpecialAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object ArcherSpecialAttackFinishedEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object ArcherAttackFinishEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PlayerHit(player = $player)"
    }

    object PriestAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PriestAttack(player = $player)"
    }

    object PriestSpecialAttackEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PriestSpecial(player = $player)"
    }

    object PriestSpecialAttackFinishEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PriestSpecial(player = $player)"
    }

    object PriestAttackFinishEvent : GameEvent (){
        lateinit var player : Entity
        override fun toString() = "PriestAttackFinish(player = $player)"
    }

    object PlayerStep: GameEvent ()  {
        lateinit var player: PlayerComponent
    }

    object UpdateMp: GameEvent ()  {
        lateinit var player: PlayerComponent
    }

    object UpdateHp: GameEvent ()  {
        lateinit var player: PlayerComponent
    }


    object BossAttack: GameEvent () {
    }

    object BossAttackFinished: GameEvent () {
        var damage = 0
        var area = Rectangle()
        override fun toString() = "BossAttack(damage=$damage, area=$area)"
    }
}

interface GameEventListener {
    fun onEvent(event: GameEvent)
}

class GameEventManager {
    private val listeners = ObjectMap<KClass<out GameEvent>, GdxSet<GameEventListener>>()

    fun addListener(type: KClass<out GameEvent>, listener: GameEventListener){
        var eventListeners = listeners[type]
        if(eventListeners == null){
            eventListeners = GdxSet()
            listeners.put(type, eventListeners)
        }
        eventListeners.add(listener)
    }

    fun removeListener(type: KClass<out GameEvent>, listener: GameEventListener) {
        listeners[type]?.remove(listener)
    }

    fun removeListener(listener: GameEventListener) {
        listeners.values().forEach{ it.remove((listener))}

    }

    fun dispatchEvent (event: GameEvent){
        listeners[event::class]?.forEach { it.onEvent(event) }
    }
}