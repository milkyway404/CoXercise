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
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.logger
import kotlin.math.*


private const val UPDATE_RATE = 1/25f
private const val SENSOR_SENSITIVITY_THRESHOLD = 4
private const val STEP_DISTANCE = 1f

private val LOG = logger<MoveSystem>()

class MoveSystem(
    private val gameEventManager: GameEventManager
) : IteratingSystem (allOf(TransformComponent::class, MoveComponent::class).exclude(RemoveComponent::class.java).get()) {

    private var accumulator = 0f
    private var magnitudePrevious = 0.0
    private var previousX = -1f;
    private var previousY = -1f;

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
        when (chosenCharacterType) {
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
        val boss = entity[BossComponent.mapper]
        if(player != null && !player.isAttacking) {

            when {
                Gdx.input.isKeyPressed(Input.Keys.W) -> {
                    movePlayer(transform,FacingDirection.NORTH)
                }
                Gdx.input.isKeyPressed(Input.Keys.A) -> {
                    movePlayer(transform,FacingDirection.WEST)
                }
                Gdx.input.isKeyPressed(Input.Keys.S) -> {
                    movePlayer(transform,FacingDirection.SOUTH)
                }
                Gdx.input.isKeyPressed(Input.Keys.D) -> {
                    movePlayer(transform, FacingDirection.EAST)
                }
            }
            val magnitude = sqrt((Gdx.input.accelerometerX.pow(2) + Gdx.input.accelerometerY.pow(2)
                    + Gdx.input.accelerometerZ.pow(2)).toDouble())
            val magnitudeDelta = magnitude - magnitudePrevious
            magnitudePrevious = magnitude

            if (magnitudeDelta >= SENSOR_SENSITIVITY_THRESHOLD) {
                Gdx.app.log("step", "TAKING A STEP")
                // player movement
                entity[FacingComponent.mapper]?.let { facing ->
                    movePlayer(transform, facing.direction)
                }
                player.mp++
                gameEventManager.dispatchEvent(GameEvent.PlayerStep.apply {
                    this.player = player
                })
            }

            movePlayerIfLocationChanged(transform);
        }else if(boss != null && boss.isAttackReady) {

            when {
                Gdx.input.isKeyPressed(Input.Keys.W) -> {
                    movePlayer(transform,FacingDirection.NORTH)
                }
                Gdx.input.isKeyPressed(Input.Keys.A) -> {
                    movePlayer(transform,FacingDirection.WEST)
                }
                Gdx.input.isKeyPressed(Input.Keys.S) -> {
                    movePlayer(transform,FacingDirection.SOUTH)
                }
                Gdx.input.isKeyPressed(Input.Keys.D) -> {
                    movePlayer(transform, FacingDirection.EAST)
                }
            }
            val magnitude = sqrt((Gdx.input.accelerometerX.pow(2) + Gdx.input.accelerometerY.pow(2)
                    + Gdx.input.accelerometerZ.pow(2)).toDouble())
            val magnitudeDelta = magnitude - magnitudePrevious
            magnitudePrevious = magnitude

            movePlayerIfLocationChanged(transform);
        }else {
            // other movement (boss, power-ups, etc)
            moveEntity(transform, move, deltaTime)
        }
    }

    private fun movePlayerIfLocationChanged(transform: TransformComponent) {
        val currentX = transform.position.x;
        val currentY = transform.position.y;
        // The +1's are to prevent floats making the player spazz around.
        if (previousX > currentX + 1) {
            movePlayer(transform, FacingDirection.WEST);
        } else if (previousX < currentX - 1) {
            movePlayer(transform, FacingDirection.EAST)
        } else if (previousY > currentY + 1) {
            movePlayer(transform, FacingDirection.NORTH)
        } else if (previousY < currentY - 1) {
            movePlayer(transform, FacingDirection.SOUTH)
        }
    }

    private fun movePlayer(
        transform:TransformComponent,
        facing:FacingDirection){

        when (facing) {
            FacingDirection.NORTH -> transform.position.y = MathUtils.clamp(
                transform.position.y + 0.25f,
                transform.size.y,
                V_HEIGHT.toFloat()
            )
            FacingDirection.SOUTH -> transform.position.y = MathUtils.clamp(
                transform.position.y - 0.25f,
                transform.size.y,
                V_HEIGHT.toFloat()
            )
            FacingDirection.EAST -> transform.position.x = MathUtils.clamp(
                transform.position.x + 0.25f,
                0f,
                V_WIDTH - transform.size.x
            )
            FacingDirection.WEST -> transform.position.x = MathUtils.clamp(
                transform.position.x - 0.25f,
                0f,
                V_WIDTH - transform.size.x
            )
        }
        previousX = transform.position.x;
        previousY = transform.position.y;
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