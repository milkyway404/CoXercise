package com.p4pProject.gameTutorial.event

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
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
        var life = 0f
        var maxLife = 0f

        override fun toString() = "PlayerHit(player = $player, life=$life, maxLife=$maxLife)"
    }

    object PlayerAttack : GameEvent (){
        lateinit var player : Entity
        var damage = 0f

        override fun toString() = "PlayerHit(player = $player, damage=$damage)"
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