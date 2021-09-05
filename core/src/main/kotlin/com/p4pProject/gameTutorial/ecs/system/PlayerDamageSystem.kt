package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
import com.p4pProject.gameTutorial.ecs.component.*
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
private val LOG = logger<PlayerDamageSystem>()

class PlayerDamageSystem (
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(BossComponent::class, TransformComponent:: class).exclude(RemoveComponent::class).get()) {

    private var attackArea : AttackArea = AttackArea(0, 0f,
        0f, 0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val boss = entity[BossComponent.mapper]
        require(boss != null ){"Entity |entity| must have a BossComponent. entity=$entity"}

            if (transform.position.x >= attackArea.startX &&
                transform.position.x <= attackArea.endX &&
                transform.position.y >= attackArea.startY &&
                transform.position.y <= attackArea.endY) {
                //ouch
                boss.hp -= attackArea.damage

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
        attackArea = AttackArea(0, 0f,
            0f, 0f, 0f)
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.WarriorSpecialAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.BossHit::class, this)
        gameEventManager.addListener(GameEvent.ArcherAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherSpecialAttackFinishedEvent::class, this)
        gameEventManager.addListener(GameEvent.PriestAttackFinishEvent::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.WarriorSpecialAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherAttackFinishEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherSpecialAttackFinishedEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestAttackFinishEvent::class, this)
    }

    override fun onEvent(event: GameEvent) {
        // player is attacking
        when (event) {
            is GameEvent.WarriorAttackEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = AttackArea(event.damage, (transform.position.x -1f),
                        (transform.position.x +1f), (transform.position.y -1f), (transform.position.y +1f))
                AttackArea(0, 0f, 0f, 0f, 0f)

            }

            is GameEvent.WarriorSpecialAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = AttackArea(event.damage, (transform.position.x -1f),
                        (transform.position.x +1f), (transform.position.y -1f), (transform.position.y +1f))
            }

            is GameEvent.ArcherAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                LOG.debug { "${player.isAttacking}" }

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = if(event.facing==FacingDirection.NORTH){
                        AttackArea(event.damage, (transform.position.x -0.5f),
                            (transform.position.x +0.5f), (transform.position.y), (transform.position.y +124f))
                    }else if(event.facing==FacingDirection.SOUTH){
                        AttackArea(event.damage, (transform.position.x -0.5f),
                            (transform.position.x +0.5f), (transform.position.y -32f), (transform.position.y))
                    }else if(event.facing==FacingDirection.EAST){
                        AttackArea(event.damage, (transform.position.x),
                            (transform.position.x +124f), (transform.position.y -0.5f), (transform.position.y +0.5f))
                    }else{
                        AttackArea(event.damage, (transform.position.x -124f),
                            (transform.position.x), (transform.position.y -0.5f), (transform.position.y +0.5f))
                    }

            }

            is GameEvent.ArcherSpecialAttackFinishedEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = if(event.facing==FacingDirection.NORTH){
                        AttackArea(event.damage, (transform.position.x -0.5f),
                            (transform.position.x +0.5f), (transform.position.y), (transform.position.y +124f))
                    }else if(event.facing==FacingDirection.SOUTH){
                        AttackArea(event.damage, (transform.position.x -0.5f),
                            (transform.position.x +0.5f), (transform.position.y -32f), (transform.position.y))
                    }else if(event.facing==FacingDirection.EAST){
                        AttackArea(event.damage, (transform.position.x),
                            (transform.position.x +124f), (transform.position.y -0.5f), (transform.position.y +0.5f))
                    }else{
                        AttackArea(event.damage, (transform.position.x -124f),
                            (transform.position.x), (transform.position.y -0.5f), (transform.position.y +0.5f))
                    }

            }

            is GameEvent.PriestAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                LOG.debug { "${player.isAttacking}" }

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = AttackArea(event.damage, (transform.position.x -32f),
                        (transform.position.x +32f), (transform.position.y -32f), (transform.position.y +32f))
            }

        }
    }

    private class AttackArea(val damage: Int, val startX: Float, val endX: Float, val startY: Float,
                                   val endY: Float)
}