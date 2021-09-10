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
import java.time.LocalDateTime
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f
private val LOG = logger<BossInputSystem>()
class BossInputSystem(
    private val gameViewport: Viewport,
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(BossComponent::class, TransformComponent::class, FacingComponent::class).get()) {
    private val tmpVec = Vector2()

    private var bossIsAttacking : Boolean = false
    private var bossIsHurt : Boolean = false
    private var bossIsStunned : Boolean = false
    private var stunnedTime : LocalDateTime = LocalDateTime.now()

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.BossAttack::class, this)
        gameEventManager.addListener(GameEvent.BossAttackFinised::class, this)
        gameEventManager.addListener(GameEvent.BossHit::class, this)
        gameEventManager.addListener(GameEvent.BossHitFinished::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.BossAttack::class, this)
        gameEventManager.removeListener(GameEvent.BossAttackFinised::class, this)
        gameEventManager.removeListener(GameEvent.BossHit::class, this)
        gameEventManager.removeListener(GameEvent.BossHitFinished::class, this)
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
            if(chosenCharacterType == CharacterType.BOSS){
                facing.direction = FacingDirection.NORTH
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            if(chosenCharacterType == CharacterType.BOSS){
                facing.direction = FacingDirection.WEST
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            if(chosenCharacterType == CharacterType.BOSS){
                facing.direction = FacingDirection.SOUTH
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            if(chosenCharacterType == CharacterType.BOSS){
                facing.direction = FacingDirection.EAST
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.J)){
            if(chosenCharacterType == CharacterType.BOSS){
                gameEventManager.dispatchEvent(GameEvent.BossAttack)
            }
        }
        boss.isAttacking = bossIsAttacking
        boss.isHurt = bossIsHurt
        boss.isStunned = bossIsStunned

        if(LocalDateTime.now().minusSeconds(4).isAfter(stunnedTime)){
            bossIsStunned = false
        }
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
        when(event){
            is GameEvent.BossAttack -> {
                bossIsAttacking = true
            }

            is GameEvent.BossAttackFinised -> {
                bossIsAttacking = false
            }

            is GameEvent.BossHit -> {
                bossIsHurt = true
                if(event.isStun){
                    bossIsStunned = true
                    stunnedTime  = LocalDateTime.now()
                }
            }

            is GameEvent.BossHitFinished -> {
                bossIsHurt = false
            }


        }
    }
}