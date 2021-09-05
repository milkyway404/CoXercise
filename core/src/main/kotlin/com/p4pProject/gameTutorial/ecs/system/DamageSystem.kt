package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
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
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.set
import java.time.LocalDateTime
import kotlin.math.max


// DO NOT LEAVE LIKE THIS
public const val DAMAGE_AREA_HEIGHT = 0f
private const val DAMAGE_PER_SECOND = 25f
private const val DEATH_EXPLOSION_DURATION = 0.9f

private val LOG = logger<PlayerInputSystem>()
class DamageSystem (
    private val gameEventManager: GameEventManager
        ) : GameEventListener, IteratingSystem(allOf(PlayerComponent::class, TransformComponent:: class).exclude(RemoveComponent::class).get()) {
    private var bossAttackAreas = BossAttackArea(0, 0F,
       0F, 0F, 0F, LocalDateTime.now(),
        0)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

            if (transform.position.x >= bossAttackAreas.startX &&
                    transform.position.x <= bossAttackAreas.endX &&
                    transform.position.y >= bossAttackAreas.startY &&
                    transform.position.y <= bossAttackAreas.endY) {
                //ouch
                player.hp -= bossAttackAreas.damage
                LOG.debug { "PlayerDamaged: ${player.characterType}" }

                gameEventManager.dispatchEvent(GameEvent.PlayerHit.apply {
                    this.player = entity
                    hp = player.hp
                    maxHp = player.maxHp
                })

                if(player.hp <= 0f){
                    entity.addComponent<RemoveComponent>(engine){
                        delay = DEATH_EXPLOSION_DURATION
                    }
                }
            }
            removeExpiredBossAttacks(bossAttackAreas);

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
                bossAttackAreas = BossAttackArea(event.damage, event.startX,
                    event.endX, event.startY, event.endY, event.startTime,
                    event.duration)
            }
        }
    }

    private fun removeExpiredBossAttacks(bossAttack: BossAttackArea) {
            if (LocalDateTime.now().minusSeconds(bossAttack.duration).isAfter(bossAttack.startTime)) {
                bossAttackAreas = BossAttackArea(0, 0F,
                    0F, 0F, 0F, LocalDateTime.now(),
                    0)
            }
    }

    private class BossAttackArea(val damage: Int, val startX: Float, val endX: Float, val startY: Float,
                                 val endY: Float, val startTime: LocalDateTime, val duration: Long)
}