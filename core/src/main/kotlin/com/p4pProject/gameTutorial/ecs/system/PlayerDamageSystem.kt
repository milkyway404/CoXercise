package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
import com.p4pProject.gameTutorial.ecs.component.BossComponent
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
private const val DAMAGE_PER_SECOND = 25f
private const val DEATH_EXPLOSION_DURATION = 0.9f

private val LOG = logger<PlayerDamageSystem>()

class PlayerDamageSystem (
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(BossComponent::class, TransformComponent:: class).exclude(RemoveComponent::class).get()) {

    private var playerAttackArea : PlayerAttackArea = PlayerAttackArea(0, 0f,
        0f, 0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val boss = entity[BossComponent.mapper]
        require(boss != null ){"Entity |entity| must have a BossComponent. entity=$entity"}

        // TODO: attach transform to player to enable multiple players

            if (transform.position.x >= playerAttackArea.startX &&
                transform.position.x <= playerAttackArea.endX &&
                transform.position.y >= playerAttackArea.startY &&
                transform.position.y <= playerAttackArea.endY) {
                //ouch
                boss.hp -= playerAttackArea.damage

                gameEventManager.dispatchEvent(GameEvent.BossHit.apply {
                    this.boss = entity
                    hp = boss.hp
                    maxHp = boss.maxHp
                })

//                if(boss.hp <= 0f){
//                    gameEventManager.dispatchEvent(GameEvent.PlayerDeath.apply {
//                        //not necessary as from dark matter
//                        this.distance = boss.distance
//                    })
//                    entity.addComponent<RemoveComponent>(engine){
//                        delay = DEATH_EXPLOSION_DURATION
//                    }
//                }
            }
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.PlayerAttack::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.PlayerAttack::class, this)
    }

    override fun onEvent(event: GameEvent) {
        // player is attacking
        when (event) {
            is GameEvent.PlayerAttack -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                LOG.debug { "${player.isAttacking}" }

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                playerAttackArea = if(!player.isAttacking){
                    PlayerAttackArea(event.damage, (transform.position.x -1f),
                        (transform.position.x +1f), (transform.position.y -1f), (transform.position.y +1f))
                }else{
                    PlayerAttackArea(0, 0f, 0f, 0f, 0f)
                }
            }
        }
    }

    private class PlayerAttackArea(val damage: Int, val startX: Float, val endX: Float, val startY: Float,
                                   val endY: Float)
}