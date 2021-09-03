package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.p4pProject.gameTutorial.V_HEIGHT
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.CURRENT_CHARACTER
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.*


private const val UPDATE_RATE = 1/25f
private const val SENSOR_SENSITIVITY_THRESHOLD = 4
private const val STEP_DISTANCE = 1f

class MoveSystem(
    private val gameEventManager: GameEventManager
) : IteratingSystem (allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class.java).get()) {

    private var accumulator = 0f
    private var magnitudePrevious = 0.0

    override fun update(deltaTime: Float) {
        accumulator +=deltaTime
        while (accumulator>= UPDATE_RATE){
            accumulator -= UPDATE_RATE

            entities.forEach{ entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    transform.prevPosition.set(transform.position)
                }
            }
            super.update(UPDATE_RATE)
        }

        val alpha = accumulator / UPDATE_RATE
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                transform.interpolatedPosition.set(
                    MathUtils.lerp(transform.prevPosition.x, transform.position.x, alpha),
                    MathUtils.lerp(transform.prevPosition.y, transform.position.y, alpha),
                    transform.position.z
                )
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // verify that the entity is actually the main player's
        when (CURRENT_CHARACTER) {
            CharacterType.WARRIOR -> {
                if (entity[WarriorAnimationComponent.mapper] == null) {
                    return
                }
            }
            CharacterType.ARCHER -> {
                if (entity[ArcherAnimationComponent.mapper] == null) {
                    return
                }
            }
            CharacterType.PRIEST -> {
                if (entity[PriestAnimationComponent.mapper] == null) {
                    return
                }
            }
        }

        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val move = entity[MoveComponent.mapper]
        require(move != null ){"Entity |entity| must have a MoveComponent. entity=$entity"}

        val player = entity[PlayerComponent.mapper]
        if(player != null && !player.isAttacking && !player.isSpecialAttacking) {

            if(Gdx.input.isKeyPressed(Input.Keys.W)){
                movePlayer(transform, move, player,FacingDirection.NORTH , deltaTime)
            }

            if(Gdx.input.isKeyPressed(Input.Keys.A)){
                movePlayer(transform, move, player,FacingDirection.WEST , deltaTime)
            }

            if(Gdx.input.isKeyPressed(Input.Keys.S)){
                movePlayer(transform, move, player,FacingDirection.SOUTH , deltaTime)
            }

            if(Gdx.input.isKeyPressed(Input.Keys.D)){
                movePlayer(transform, move, player,FacingDirection.EAST , deltaTime)
            }
            val magnitude = sqrt((Gdx.input.accelerometerX.pow(2) + Gdx.input.accelerometerY.pow(2)
                    + Gdx.input.accelerometerZ.pow(2)).toDouble())
            val magnitudeDelta = magnitude - magnitudePrevious
            magnitudePrevious = magnitude

            if (magnitudeDelta >= SENSOR_SENSITIVITY_THRESHOLD) {
                Gdx.app.log("step", "TAKING A STEP")
                // player movement
                entity[FacingComponent.mapper]?.let { facing ->
                    movePlayer(transform, move, player, facing.direction, deltaTime)
                }
                player.mp++
                gameEventManager.dispatchEvent(GameEvent.PlayerStep.apply {
                    this.player = player
                })

            }
        }else {
            // other movement (boss, power-ups, etc)
            moveEntity(transform, move, deltaTime)
        }
    }

    private fun movePlayer(
        transform:TransformComponent,
        move:MoveComponent,
        player:PlayerComponent,
        facing:FacingDirection,
        deltaTime:Float){

        Gdx.app.log("direction", facing.toString())
        when (facing) {
            FacingDirection.NORTH -> transform.position.y = MathUtils.clamp(
                transform.position.y + 1,
                0f,
                V_WIDTH - transform.size.y
            )
            FacingDirection.SOUTH -> transform.position.y = MathUtils.clamp(
                transform.position.y - 1,
                0f,
                V_WIDTH - transform.size.y
            )
            FacingDirection.EAST -> transform.position.x = MathUtils.clamp(
                transform.position.x + 1,
                0f,
                V_WIDTH - transform.size.x
            )
            FacingDirection.WEST -> transform.position.x = MathUtils.clamp(
                transform.position.x - 1,
                0f,
                V_WIDTH - transform.size.x
            )
        }
        Gdx.app.log("POSITION", "x: " + transform.position.x + ", y: " + transform.position.y)
    }

    private fun moveEntity (transform: TransformComponent, move: MoveComponent, deltaTime: Float){

        // Clamping allows players movement to be only within the screen
        transform.position.x = MathUtils.clamp(
            transform.position.x + move.speed.x * deltaTime,
            0f,
            V_WIDTH - transform.size.x,
        )

        transform.position.y = MathUtils.clamp(
            transform.position.y + move.speed.y * deltaTime,
            1f,
            V_HEIGHT + 1f - transform.size.y,
        )
    }

}