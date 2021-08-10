package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.RemoveComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.event.GameEventManager
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.debug
import ktx.preferences.flush
import ktx.preferences.set
import java.time.LocalDateTime
import kotlin.math.max


// DO NOT LEAVE LIKE THIS
public const val DAMAGE_AREA_HEIGHT = 0f
private const val DAMAGE_PER_SECOND = 25f
private const val DEATH_EXPLOSION_DURATION = 0.9f

class DamageSystem (
    private val gameEventManager: GameEventManager
        ) : GameEventListener, IteratingSystem(allOf(PlayerComponent::class, TransformComponent:: class).exclude(RemoveComponent::class).get()) {

    private val bossAttackAreas = ArrayList<BossAttackArea>()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

        // TODO: attach transform to player to enable multiple players


        removeExpiredBossAttacks();

        // This needs to be rewritten
        if(transform.position.y <= DAMAGE_AREA_HEIGHT){
            var damage = DAMAGE_PER_SECOND * deltaTime
            if(player.shield> 0f){
                val blockAmount = player.shield
                player.shield = max(0f, player.shield - damage)
                damage -= blockAmount

                if(damage <= 0f){
                    // all damage is blocked
                    return
                }
            }

            player.life -= damage
            gameEventManager.dispatchEvent(GameEvent.PlayerHit.apply {
                this.player = entity
                life = player.life
                maxLife = player.maxLife
            })
            if(player.life <= 0f){
                gameEventManager.dispatchEvent(GameEvent.PlayerDeath.apply {
                    this.distance = player.distance
                })
                entity.addComponent<RemoveComponent>(engine){
                    delay = DEATH_EXPLOSION_DURATION
                }
            }
        }
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.BossAttack::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.BossAttack::class, this)
    }

    override fun onEvent(event: GameEvent) {
        // boss is attacking lol
        when (event) {
            is GameEvent.BossAttack -> {
                bossAttackAreas.add(BossAttackArea(event.startX, event.endX,
                    event.startY, event.endY, event.startTime, event.duration))
            }
        }
    }

    private fun removeExpiredBossAttacks() {
        for (bossAttack in bossAttackAreas) {
            if (LocalDateTime.now().minusSeconds(bossAttack.duration).isAfter(bossAttack.startTime)) {
                bossAttackAreas.remove(bossAttack);
            }
        }
    }

    private class BossAttackArea(val startX: Int, val endX: Int, val startY: Int, val endY: Int,
        val startTime: LocalDateTime, val duration: Long) {
    }
}