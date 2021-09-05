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
import com.p4pProject.gameTutorial.screen.CURRENT_CHARACTER
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.logger
import java.time.LocalDateTime
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f
private val LOG = logger<BossInputSystem>()
class BossInputSystem(
    private val gameViewport: Viewport,
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(BossComponent::class, TransformComponent::class, FacingComponent::class).get()) {
    private val tmpVec = Vector2()

    private var playerIsAttacking : Boolean = false

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity[FacingComponent.mapper]
        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$entity" }

        val transform = entity[TransformComponent.mapper]
        require(transform != null) { "Entity |entity| must have a TransformComponent. entity=$entity" }

        val boss = entity[BossComponent.mapper]
        require(boss != null) { "Entity |entity| must have a PlayerComponent. entity=$entity" }


        //This part is the part that is the control
        tmpVec.x = Gdx.input.x.toFloat()
        gameViewport.unproject(tmpVec)
        val diffX = tmpVec.x - transform.position.x - transform.size.x * 0.5f

        //facing.direction = getFacingDirection()

        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            if(CURRENT_CHARACTER == CharacterType.BOSS){
                facing.direction = FacingDirection.NORTH
            }

        }

        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            if(CURRENT_CHARACTER == CharacterType.BOSS){
                facing.direction = FacingDirection.WEST
            }

        }

        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            if(CURRENT_CHARACTER == CharacterType.BOSS){
                facing.direction = FacingDirection.SOUTH
            }

        }

        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            if(CURRENT_CHARACTER == CharacterType.BOSS){
                facing.direction = FacingDirection.EAST
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.J)){
            if(CURRENT_CHARACTER == CharacterType.BOSS){
                gameEventManager.dispatchEvent(GameEvent.BossAttack.apply {
            this.damage = 0
            this.startX = transform.position.x - 1f
            this.endX = transform.position.x + 1f
            this.startY = transform.position.y - 1f
            this.endY = transform.position.y + 1f
            this.startTime = LocalDateTime.now()
            this.duration = 0.1.toLong()
        })
            }
        }



        boss.isAttacking = playerIsAttacking
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
        TODO("Not yet implemented")
    }
}