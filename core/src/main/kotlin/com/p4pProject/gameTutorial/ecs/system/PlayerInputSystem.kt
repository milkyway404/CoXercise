package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.logger
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f
private val LOG = logger<PlayerInputSystem>()
class PlayerInputSystem(
    private val gameViewport: Viewport,
    private val gameEventManager: GameEventManager
    ) : GameEventListener, IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()) {
    private val tmpVec = Vector2()

    private var warriorIsAttacking : Boolean = false
    private var warriorIsSpecialAttacking : Boolean = false

    private var archerIsAttacking : Boolean = false
    private var archerIsSpecialAttacking : Boolean = false

    private var priestIsAttacking : Boolean = false
    private var priestIsSpecialAttacking : Boolean = false


    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.WarriorAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.WarriorSpecialAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.WarriorSpecialAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherSpecialAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherSpecialAttackFinishedEvent::class, this)
        gameEventManager.addListener(GameEvent.PriestAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.PriestAttackFinishEvent::class, this)
        gameEventManager.addListener(GameEvent.PriestSpecialAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.PriestSpecialAttackFinishEvent::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.WarriorAttackFinishEvent::class, this)
        gameEventManager.removeListener(GameEvent.WarriorSpecialAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.WarriorSpecialAttackFinishEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherSpecialAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherAttackFinishEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherSpecialAttackFinishedEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestAttackFinishEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestSpecialAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.PriestSpecialAttackFinishEvent::class, this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {

        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }

        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a TransformComponent. entity=$entity" }

        val player = entity[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$entity" }


        //This part is the part that is the control
        tmpVec.x = Gdx.input.x.toFloat()
        gameViewport.unproject(tmpVec)
        val diffX = tmpVec.x - transform.position.x - transform.size.x * 0.5f

        //facing.direction = getFacingDirection(facing.direction, player)

        if(Gdx.input.isKeyPressed(Input.Keys.W) && chosenCharacterType == player.characterType && !player.isDead){
            facing.direction = FacingDirection.NORTH
        }

        if(Gdx.input.isKeyPressed(Input.Keys.A) && chosenCharacterType == player.characterType && !player.isDead){
            facing.direction = FacingDirection.WEST

        }

        if(Gdx.input.isKeyPressed(Input.Keys.S) && chosenCharacterType == player.characterType && !player.isDead){
            facing.direction = FacingDirection.SOUTH

        }

        if(Gdx.input.isKeyPressed(Input.Keys.D) && chosenCharacterType == player.characterType && !player.isDead){
            facing.direction = FacingDirection.EAST

        }

        when (player.characterType) {
            CharacterType.WARRIOR -> {
                player.isAttacking = warriorIsAttacking
                player.isSpecialAttacking = warriorIsSpecialAttacking
            }
            CharacterType.ARCHER -> {
                player.isAttacking = archerIsAttacking
                player.isSpecialAttacking = archerIsSpecialAttacking
            }
            CharacterType.PRIEST -> {
                player.isAttacking = priestIsAttacking
                player.isSpecialAttacking = priestIsSpecialAttacking
            }
        }
    }

    private fun getFacingDirection(preFacing: FacingDirection, player : PlayerComponent): FacingDirection {
        val angle: Int = Gdx.input.azimuth.toDouble().roundToInt()
        return if (angle in -7..7) {
            FacingDirection.NORTH
        } else if (angle in 83..97) {
            FacingDirection.EAST
        } else if (angle in 173..180 || angle in -180..-173) {
            FacingDirection.SOUTH
        } else if (angle in -97..-83) {
            FacingDirection.WEST
        } else {
            preFacing
        }
    }

    override fun onEvent(event: GameEvent) {
        when(event){
            is GameEvent.WarriorAttackEvent ->{
                warriorIsAttacking = true
            }
            is GameEvent.ArcherAttackEvent ->{
                archerIsAttacking = true
            }
            is GameEvent.PriestAttackEvent ->{
                priestIsAttacking = true
            }
            is GameEvent.WarriorAttackFinishEvent ->{
                warriorIsAttacking = false
            }
            is GameEvent.ArcherAttackFinishEvent ->{
                archerIsAttacking = false
            }
            is GameEvent.PriestAttackFinishEvent ->{
                priestIsAttacking = false
            }
            is GameEvent.PriestSpecialAttackFinishEvent ->{
                priestIsSpecialAttacking = false
            }
            is GameEvent.WarriorSpecialAttackEvent ->{
                warriorIsSpecialAttacking = true
            }
            is GameEvent.ArcherSpecialAttackEvent -> {
                archerIsSpecialAttacking = true
            }
            is GameEvent.PriestSpecialAttackEvent ->{
                priestIsSpecialAttacking = true
            }
            is GameEvent.WarriorSpecialAttackFinishEvent ->{
                warriorIsSpecialAttacking = false
            }
            is GameEvent.ArcherSpecialAttackFinishedEvent ->{
                archerIsSpecialAttacking = false
            }
        }

    }
}