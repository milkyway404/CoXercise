package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.CharacterType
import com.p4pProject.gameTutorial.screen.GameScreen
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


private val LOG = logger<HealSystem>()

class HealSystem (
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(PlayerComponent::class).exclude(RemoveComponent::class).get()) {

    var warriorHealed = true;
    var archerHealed = true;
    var priestHealed = true;

    override fun processEntity(entity: Entity, deltaTime: Float) {

        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a BossComponent. entity=$entity"}
        if(player.characterType == CharacterType.WARRIOR && !warriorHealed){
            player.hp = minOf(player.hp + 25, player.maxHp)
            player.isDead = false
            LOG.debug { "warrior hp: ${player.hp}" }
            warriorHealed = true;
            gameEventManager.dispatchEvent(GameEvent.UpdateHp.apply {
                this.player = player
            })
        }

        else if(player.characterType == CharacterType.ARCHER && !archerHealed){
            player.hp = minOf(player.hp + 25, player.maxHp)
            player.isDead = false
            LOG.debug { "archer hp: ${player.hp}" }
            archerHealed = true
            gameEventManager.dispatchEvent(GameEvent.UpdateHp.apply {
                this.player = player
            })
        }

        else if(player.characterType == CharacterType.PRIEST && !priestHealed){
            player.hp = minOf(player.hp + 25, player.maxHp)
            player.isDead = false
            LOG.debug { "priest hp: ${player.hp}" }
            priestHealed = true
            gameEventManager.dispatchEvent(GameEvent.UpdateHp.apply {
                this.player = player
            })
        }



    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)

        gameEventManager.addListener(GameEvent.PriestSpecialAttackFinishEvent::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.PriestSpecialAttackFinishEvent::class, this)
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PriestSpecialAttackFinishEvent) {
            Gdx.app.log("Priest", "Special Attack")
            warriorHealed = false
            priestHealed = false
            archerHealed = false
        }
    }
}