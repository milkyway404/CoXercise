package com.p4pProject.gameTutorial.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.p4pProject.gameTutorial.ecs.component.FacingComponent
import com.p4pProject.gameTutorial.ecs.component.FacingDirection
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.PowerUpType
import ktx.collections.GdxSet
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass


sealed class GameEvent{
    object PlayerDeath : GameEvent (){
        var distance = 0f

        override fun toString() = "PlayerDeath(distance = ${distance})"
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

        override fun toString() = "PlayerHit(player = $boss, hp=$hp, maxHp=$maxHp)"
    }

    object WarriorAttackEvent : GameEvent (){
        lateinit var player : Entity
        var damage = 0
        override fun toString() = "PlayerHit(player = $player, damage=$damage)"
    }

    object WarriorAttackFinishEvent : GameEvent (){
    }

    object WarriorSpecialAttackEvent : GameEvent (){
        lateinit var player : Entity
        var damage = 0
        override fun toString() = "PlayerHit(player = $player, damage=$damage)"
    }

    object ArcherAttackEvent : GameEvent (){
        lateinit var player : Entity
        lateinit var facing : FacingDirection
        var damage = 0
        override fun toString() = "PlayerHit(player = $player, damage=$damage)"
    }

    object ArcherAttackFinishEvent : GameEvent (){
    }

    object PriestAttackEvent : GameEvent (){
        lateinit var player : Entity
        var damage = 0
        override fun toString() = "PlayerHit(player = $player, damage=$damage)"
    }

    object PriestSpecialAttackEvent : GameEvent (){
        lateinit var player : Entity
        var healing = 0
        override fun toString() = "PlayerHit(player = $player, damage=$healing)"
    }

    object PriestAttackFinishEvent : GameEvent (){
    }

    object PlayerStep: GameEvent ()  {
        lateinit var player: PlayerComponent
    }

    object UpdateMp: GameEvent ()  {
        lateinit var player: PlayerComponent
    }


    object BossAttack: GameEvent () {
        var damage = 0
        var startX = 0
        var endX = 0
        var startY = 0
        var endY = 0
        var startTime: LocalDateTime = LocalDateTime.now()
        var duration = 0L

        override fun toString() = "BossAttack(damage=$damage, startX=$startX, " +
                "endX=$endX, startY=$startY, endY=$endY, startTime=$startTime," +
                "duration=$duration)"
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