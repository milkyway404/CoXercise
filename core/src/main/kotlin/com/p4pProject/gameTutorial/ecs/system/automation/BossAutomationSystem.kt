package com.p4pProject.gameTutorial.ecs.system.automation

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.V_HEIGHT
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.log.logger
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TOUCH_TOLERANCE_DISTANCE = 0.3f
private const val HIGH_HEALTH_THRESHOLD = 200
private const val MEDIUM_HEALTH_THRESHOLD = 100
const val BOSS_ATTACK_RANGE = 0f

private val LOG = logger<BossAutomationSystem>()
class BossAutomationSystem(
    private val gameViewport: Viewport,
    private val gameEventManager: GameEventManager
) : GameEventListener, IteratingSystem(allOf(BossComponent::class, TransformComponent::class, FacingComponent::class, PlayerInfoComponent::class).exclude(RemoveComponent::class).get()) {
    private val tmpVec = Vector2()

    private var bossIsReadyToAttack : Boolean = false
    private var bossIsAttacking :Boolean = false
    private var bossIsHurt : Boolean = false
    private var bossIsStunned : Boolean = false
    private var stunnedTime : LocalDateTime = LocalDateTime.now()

    private var movementSpeed: Float = 0f

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.BossAttack::class, this)
        gameEventManager.addListener(GameEvent.BossAttackFinished::class, this)
        gameEventManager.addListener(GameEvent.BossHit::class, this)
        gameEventManager.addListener(GameEvent.BossHitFinished::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.BossAttack::class, this)
        gameEventManager.removeListener(GameEvent.BossAttackFinished::class, this)
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

        val characterToAttack = when (boss.hp){
            in HIGH_HEALTH_THRESHOLD..Int.MAX_VALUE -> {
                movementSpeed = 0.08f
                boss.attackDamage = 5
                findClosestCharacter(entity)
            }
            in MEDIUM_HEALTH_THRESHOLD..HIGH_HEALTH_THRESHOLD -> {
                movementSpeed = 0.1f
                boss.attackDamage = 10
                findClosestCharacter(entity)
            }
            else -> {
                movementSpeed = 0.12f
                boss.attackDamage = 20
                findLowestHpCharacter(entity)
            }
        }

        walkToAndAttackCharacter(entity, characterToAttack)

        //This part is the part that is the control
        tmpVec.x = Gdx.input.x.toFloat()
        gameViewport.unproject(tmpVec)

        boss.isAttackReady = bossIsReadyToAttack
        boss.isAttacking = bossIsAttacking
        boss.isHurt = bossIsHurt
        boss.isStunned = bossIsStunned

        if(LocalDateTime.now().minusSeconds(4).isAfter(stunnedTime)){
            bossIsStunned = false
        }
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.BossAttackFinished) {
//            Gdx.app.log("Boss Attack", "Finished")
            bossIsReadyToAttack = false
            bossIsAttacking = false
        } else if (event is GameEvent.BossHit) {
            bossIsHurt = true
            if(event.isStun){
                bossIsStunned = true
                stunnedTime  = LocalDateTime.now()
            }
        } else if (event is GameEvent.BossHitFinished) {
            bossIsHurt = false
        }
    }

    private fun findClosestCharacter(boss: Entity): Entity {
        val playerInfo = boss[PlayerInfoComponent.mapper]!!
        val bossPos = boss[TransformComponent.mapper]!!
        return playerInfo.getClosestCharacter(bossPos.position)
    }

    private fun findLowestHpCharacter(boss: Entity): Entity {
        val playerInfo = boss[PlayerInfoComponent.mapper]!!
        return playerInfo.getLowestHpCharacter()
    }

    private fun walkToAndAttackCharacter(boss: Entity, characterToAttack: Entity) {
        if (bossIsReadyToAttack || boss[BossComponent.mapper]!!.isAttacking || bossIsHurt || bossIsStunned) {
            //Gdx.app.log("Checks", bossIsReadyToAttack.toString() + boss[BossComponent.mapper]!!.isAttacking.toString())
            return;
        }

        val facing = boss[FacingComponent.mapper]!!

        facing.direction = getDirectionCharacterAt(characterToAttack, boss)

        if (isCharacterInAttackRange(characterToAttack, boss)) {
            // attack
//            Gdx.app.log("Attack", "meow")
            bossIsReadyToAttack = true
            //boss[PlayerInfoComponent.mapper]!!.printPlayerHps()
        } else {
            // walk
//            Gdx.app.log("Walk", "meow")
            walkToCharacter(boss, facing.direction)
        }
    }

    private fun getDirectionCharacterAt(character: Entity, boss: Entity): FacingDirection {
        val bossPos = boss[TransformComponent.mapper]!!.position
        val characterPos = character[TransformComponent.mapper]!!.position

        // go in the direction of the longest distance
        val disX = abs(bossPos.x - characterPos.x)
        val disY = abs(bossPos.y - characterPos.y)

//        Gdx.app.log("Boss Position", "x: " + bossPos.x.toString() + ", y: " + bossPos.y.toString())
//        Gdx.app.log("Character Position", "x: " + characterPos.x + ",y: " + characterPos.y)

        return if (disX > disY) {
            if (bossPos.x > characterPos.x) {
                FacingDirection.WEST
            } else {
                FacingDirection.EAST
            }
        } else {
            if (bossPos.y > characterPos.y) {
                FacingDirection.SOUTH
            } else {
                FacingDirection.NORTH
            }
        }

    }

    private fun walkToCharacter(boss: Entity, facingDirection: FacingDirection) {
        val bossPos = boss[TransformComponent.mapper]!!.position

        when (facingDirection) {
            FacingDirection.NORTH -> {
                bossPos.y = MathUtils.clamp(bossPos.y + movementSpeed, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.SOUTH -> {
                bossPos.y = MathUtils.clamp(bossPos.y - movementSpeed, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.EAST -> {
                bossPos.x = MathUtils.clamp(bossPos.x + movementSpeed, 0f, V_WIDTH.toFloat())
            }
            FacingDirection.WEST -> {
                bossPos.x = MathUtils.clamp(bossPos.x - movementSpeed, 0f, V_WIDTH.toFloat())
            }
        }
    }

    private fun isCharacterInAttackRange(character: Entity, boss: Entity): Boolean {
        val bossTrans = boss[TransformComponent.mapper]!!
        val characterTrans = character[TransformComponent.mapper]!!

//        Gdx.app.log("Character To Attack", character[PlayerComponent.mapper]!!.characterType.toString())
//        Gdx.app.log("Character Pos", characterTrans.position.x.toString() + ", " + characterTrans.position.y.toString())
//        Gdx.app.log("Boss Pos", bossTrans.position.x.toString() + ", " + bossTrans.position.y.toString())
        return (characterTrans.overlaps(bossTrans))
    }
}