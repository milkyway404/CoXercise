package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.ecs.component.FacingComponent
import com.p4pProject.gameTutorial.ecs.component.FacingDirection
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f

class PlayerInputSystem(
    private val gameViewport: Viewport
    ) : IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()) {
    private val tmpVec = Vector2()
    private var prevDirection = FacingDirection.NORTH

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }
        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a TransformComponent. entity=$entity" }

        //This part is the part that is the control
        tmpVec.x = Gdx.input.x.toFloat()
        gameViewport.unproject(tmpVec)
        val diffX = tmpVec.x - transform.position.x - transform.size.x * 0.5f
        facing.direction = getFacingDirection()
    }

    private fun getFacingDirection(): FacingDirection {
        val angle = Gdx.input.azimuth.toDouble().roundToInt()

        var currentDirection = when {
            (angle in -40..40) -> FacingDirection.NORTH

            // NorthEast Buffer
            (angle in 41..50 && prevDirection === FacingDirection.NORTH) -> FacingDirection.NORTH
            (angle in 41..50 && prevDirection === FacingDirection.EAST) -> FacingDirection.EAST
            (angle in 41..45) -> FacingDirection.NORTH
            (angle in 46..50) -> FacingDirection.EAST

            (angle in 51..130) -> FacingDirection.EAST

            // SouthEast Buffer
            (angle in 131..140 && prevDirection === FacingDirection.EAST) -> FacingDirection.EAST
            (angle in 131..140 && prevDirection === FacingDirection.SOUTH) -> FacingDirection.SOUTH
            (angle in 131..135) -> FacingDirection.EAST
            (angle in 136..140) -> FacingDirection.SOUTH

            (angle in 141..180 || angle in -180..-140) -> FacingDirection.SOUTH

            // SouthWest Buffer
            (angle in -139..130 && prevDirection === FacingDirection.SOUTH) -> FacingDirection.SOUTH
            (angle in -139..130 && prevDirection === FacingDirection.WEST) -> FacingDirection.WEST
            (angle in -139..135) -> FacingDirection.SOUTH
            (angle in -134..130) -> FacingDirection.WEST

            (angle in -129..-51) -> FacingDirection.WEST

            // NorthWest Buffer
            (angle in -50..-41 && prevDirection === FacingDirection.NORTH) -> FacingDirection.NORTH
            (angle in -50..-41 && prevDirection === FacingDirection.WEST) -> FacingDirection.WEST
            (angle in -50..46) -> FacingDirection.WEST
            (angle in -45..41) -> FacingDirection.NORTH

            else -> FacingDirection.NORTH
        }

        prevDirection = currentDirection

        return currentDirection
    }
}