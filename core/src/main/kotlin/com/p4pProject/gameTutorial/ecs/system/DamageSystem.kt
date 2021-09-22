package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.RemoveComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.CharacterType
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.debug
import ktx.log.logger

private const val DEATH_EXPLOSION_DURATION = 0.9f

private val LOG = logger<DamageSystem>()
class DamageSystem (
    private val gameEventManager: GameEventManager
        ) : GameEventListener, IteratingSystem(allOf(PlayerComponent::class, TransformComponent:: class).exclude(RemoveComponent::class).get()) {

    private var bossAttackArea = BossAttackArea(0, Rectangle())

    var warriorCheck = false;
    var archerCheck = false;
    var priestCheck = false;

    var warriorDead = false
    var archerDead = false
    var priestDead = false

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

        if(!warriorCheck && player.characterType == CharacterType.WARRIOR){
            warriorCheck = true;
            checkDmg(entity);
        }else if(!archerCheck && player.characterType == CharacterType.SLINGER){
            archerCheck = true;
            checkDmg(entity);
        }else if(!priestCheck && player.characterType == CharacterType.NECROMANCER){
            priestCheck = true;
            checkDmg(entity);
        }

        if((warriorCheck || warriorDead) && (archerCheck || archerDead) && (priestCheck || priestDead)){
            bossAttackArea = BossAttackArea(0, Rectangle())
            warriorCheck = false;
            archerCheck = false;
            priestCheck = false;
        }
    }

    private fun checkDmg(entity: Entity){
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}


        if (transform.overlapsRect(bossAttackArea.area) && !player.isDead) {
            //ouch
            player.hp -= bossAttackArea.damage
            if(player.hp <= 0f){
                player.hp = 0
            }
            LOG.debug { "PlayerDamaged: ${player.characterType}" }

            if(player.characterType == chosenCharacterType){
                gameEventManager.dispatchEvent(GameEvent.PlayerHit.apply {
                    this.player = entity
                    hp = player.hp
                    maxHp = player.maxHp
                })
            }

            if(player.hp <= 0f){
                player.isDead = true
//                entity.addComponent<RemoveComponent>(engine){
//                    delay = DEATH_EXPLOSION_DURATION
//                }
                gameEventManager.dispatchEvent(GameEvent.PlayerDeath.apply {
                    this.characterType = player.characterType
                })
            }
        }
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.BossAttackFinished::class, this)
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.BossAttackFinished::class, this)
        gameEventManager.removeListener(GameEvent.PlayerDeath::class, this)
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.BossAttackFinished) {
            bossAttackArea = BossAttackArea(event.damage, event.area)
        }
        if (event is GameEvent.PlayerDeath) {
            LOG.debug { "ded: ${event.characterType }"}
            when(event.characterType) {
                CharacterType.WARRIOR -> warriorDead = true
                CharacterType.SLINGER -> archerDead = true
                CharacterType.NECROMANCER -> priestDead = true
            }
        }
    }


    private data class BossAttackArea(val damage: Int, val area: Rectangle)
}