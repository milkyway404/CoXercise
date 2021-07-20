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
import ktx.log.debug
import ktx.log.logger
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f
private val LOG = logger<PlayerInputSystem>()
class PlayerInputSystem(
    private val gameViewport: Viewport
    ) : IteratingSystem(allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()) {
    private val tmpVec = Vector2()

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
}