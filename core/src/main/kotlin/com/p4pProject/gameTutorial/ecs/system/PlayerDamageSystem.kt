package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.ecs.system.automation.ARCHER_ATTACK_RANGE
import com.p4pProject.gameTutorial.ecs.system.automation.PRIEST_ATTACK_RANGE
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
        0f, 0f, 0f, false)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val boss = entity[BossComponent.mapper]
        require(boss != null ){"Entity |entity| must have a BossComponent. entity=$entity"}

        val playerAttackBoundingRect = Rectangle().set(
            attackArea.startX,
            attackArea.startY,
            attackArea.endX - attackArea.startX,
            attackArea.endY - attackArea.startY
        )

        if (transform.overlapsRect(playerAttackBoundingRect)) {
            //ouch
            boss.hp -= attackArea.damage
            if(boss.hp <=0){
                gameEventManager.dispatchEvent(GameEvent.BossDead)
            }


            if(attackArea.isStun){
                gameEventManager.dispatchEvent(GameEvent.BossHit.apply {
                    this.boss = entity
                    hp = boss.hp
                    maxHp = boss.maxHp
                    isStun = true
                })
            }else{
                gameEventManager.dispatchEvent(GameEvent.BossHit.apply {
                    this.boss = entity
                    hp = boss.hp
                    maxHp = boss.maxHp
                    isStun = false
                })
            }

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
            0f, 0f, 0f, false)
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
                attackArea = AttackArea(player.normalAttackDamage, (transform.position.x),
                    (transform.position.x + transform.size.x), (transform.position.y), (transform.size.y), false)

            }

            is GameEvent.WarriorSpecialAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                Gdx.app.log("Warrior", "Special Attack")
                attackArea = AttackArea(player.specialAttackDamageOrHeal, (transform.position.x),
                    (transform.position.x + transform.size.x), (transform.position.y), (transform.position.y + transform.size.y), true)
            }

            is GameEvent.ArcherAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}
                val facing = event.player[FacingComponent.mapper]
                require(facing != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = AttackArea(player.normalAttackDamage, (transform.position.x - ARCHER_ATTACK_RANGE),
                        (transform.position.x + ARCHER_ATTACK_RANGE), (transform.position.y - ARCHER_ATTACK_RANGE), (transform.position.y + ARCHER_ATTACK_RANGE), false)

            }

            is GameEvent.ArcherSpecialAttackFinishedEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                Gdx.app.log("Archer", "Special Attack")
                attackArea = AttackArea(player.specialAttackDamageOrHeal, (transform.position.x - ARCHER_ATTACK_RANGE),
                        (transform.position.x + ARCHER_ATTACK_RANGE), (transform.position.y - ARCHER_ATTACK_RANGE), (transform.position.y + ARCHER_ATTACK_RANGE), false)
            }

            is GameEvent.PriestAttackFinishEvent -> {
                val transform = event.player[TransformComponent.mapper]
                require(transform != null ){"Entity |entity| must have a TransformComponent. entity=${event.player}"}
                val player = event.player[PlayerComponent.mapper]
                require(player != null ){"Entity |entity| must have a PlayerComponent. entity=${event.player}"}

                LOG.debug { "${player.isAttacking}" }

                //SHOULD BE FIXED!!!!!
                //TEMPORARY SOLUTION
                attackArea = AttackArea(player.normalAttackDamage, (transform.position.x - PRIEST_ATTACK_RANGE),
                    (transform.position.x + PRIEST_ATTACK_RANGE), (transform.position.y - PRIEST_ATTACK_RANGE), (transform.position.y + PRIEST_ATTACK_RANGE), false)
            }

        }
    }

    private class AttackArea(val damage: Int, val startX: Float, val endX: Float, val startY: Float,
                             val endY: Float, val isStun : Boolean)
}