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

    private var playerIsAttacking : Boolean = false

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.addListener(GameEvent.ArcherAttackEvent::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.WarriorAttackEvent::class, this)
        gameEventManager.removeListener(GameEvent.ArcherAttackEvent::class, this)
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

        //facing.direction = getFacingDirection()

        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            facing.direction = FacingDirection.NORTH
        }

        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            facing.direction = FacingDirection.WEST
        }

        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            facing.direction = FacingDirection.SOUTH
        }

        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            facing.direction = FacingDirection.EAST
        }

        player.isAttacking = playerIsAttacking
    }

    private fun getFacingDirection(): FacingDirection {
        val angle = Gdx.input.azimuth.toDouble().roundToInt()
        return if (angle in -45..45) {
            FacingDirection.NORTH
        } else if (angle in 46..135) {
            FacingDirection.EAST
        } else if (angle in 136..180 || angle in -180..-135) {
            FacingDirection.SOUTH
        } else if (angle in -134..-46) {
            FacingDirection.WEST
        } else {
            FacingDirection.NORTH
        }
    }

    override fun onEvent(event: GameEvent) {
        playerIsAttacking = !playerIsAttacking
    }
}