package com.p4pProject.gameTutorial.ecs.system.automation

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.p4pProject.gameTutorial.V_HEIGHT
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.CharacterType
import com.p4pProject.gameTutorial.screen.GameMode
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import com.p4pProject.gameTutorial.screen.gameMode
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.abs

const val ARCHER_MOVEMENT_SPEED = 0.25f
const val ARCHER_ATTACK_RANGE = 5f

class ArcherAutomationSystem(
    private val gameEventManager: GameEventManager): IteratingSystem(allOf(PlayerComponent::class,
    TransformComponent::class, FacingComponent::class, ArcherAnimationComponent::class).get()) {

    private var prevX = -1f
    private var prevY = -1f

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$entity" }

        if (gameMode == GameMode.MULTIPLAYER || player.characterType != CharacterType.ARCHER ||
            chosenCharacterType == CharacterType.ARCHER) {
            return
        }

        if(player.characterType == CharacterType.ARCHER && player.isDead){
            return
        }

        walkToRunAwayAndAttackBoss(entity)
    }

    private fun walkToRunAwayAndAttackBoss(archer: Entity) {

        val player = archer[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$archer" }

        val facing = archer[FacingComponent.mapper]
        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$facing" }

        if (player.isAttacking || player.isSpecialAttacking) {
            return
        }

        val bossInfo = archer[BossInfoComponent.mapper]
        if (bossInfo == null || !bossInfo.bossIsInitialized()) {
            return
        }


        val bossTrans = bossInfo.boss[TransformComponent.mapper]!!
        val archerTrans = archer[TransformComponent.mapper]!!

        val isInSameLocation = isInSameLocation(archerTrans)

        when {
            player.hp < player.maxHp * 0.2 && !isInSameLocation -> {
                facing.direction = findBossDirection(bossTrans, archerTrans, faceBoss = false)
                move(archerTrans, facing.direction)
            }
            isBossInAttackRange(archerTrans, bossTrans) -> {
                facing.direction = findBossDirection(bossTrans, archerTrans, faceBoss = true)
                attackBoss(archer)
            }
            else -> {
                facing.direction = findBossDirection(bossTrans, archerTrans, faceBoss = true)
                move(archerTrans, facing.direction)
            }
        }
    }

    private fun isInSameLocation(archerTrans: TransformComponent): Boolean {
        return if (prevX == archerTrans.position.x && prevY == archerTrans.position.y) {
            true
        } else {
            prevX = archerTrans.position.x
            prevY = archerTrans.position.y
            false
        }
    }

    private fun isBossInAttackRange(archerTrans: TransformComponent, bossTrans: TransformComponent): Boolean {
        return archerTrans.position.dst(bossTrans.position) < ARCHER_ATTACK_RANGE
    }

    private fun attackBoss(archer: Entity) {
        val player = archer[PlayerComponent.mapper]!!

        if (player.mp >= player.specialAttackMpCost) {
            player.mp -= player.specialAttackMpCost
            gameEventManager.dispatchEvent(GameEvent.ArcherSpecialAttackEvent.apply {
                this.player = archer
            })
        } else {
            gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                this.player = archer
            })
        }
    }

    private fun move(archerTrans: TransformComponent, facingDirection: FacingDirection) {
        val archerPos = archerTrans.position

        when (facingDirection) {
            FacingDirection.NORTH -> {
                archerPos.y = MathUtils.clamp(archerPos.y + ARCHER_MOVEMENT_SPEED, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.SOUTH -> {
                archerPos.y = MathUtils.clamp(archerPos.y - ARCHER_MOVEMENT_SPEED, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.EAST -> {
                archerPos.x = MathUtils.clamp(archerPos.x + ARCHER_MOVEMENT_SPEED, 0f, V_WIDTH - archerTrans.size.x)
            }
            FacingDirection.WEST -> {
                archerPos.x = MathUtils.clamp(archerPos.x - ARCHER_MOVEMENT_SPEED, 0f, V_WIDTH - archerTrans.size.x)
            }
        }
    }

    private fun findBossDirection(bossTrans: TransformComponent, archerTrans: TransformComponent, faceBoss: Boolean): FacingDirection {
        val bossPos = bossTrans.position
        val archerPos = archerTrans.position

        // go in the direction of the longest distance
        val disX = abs(bossPos.x - archerPos.x)
        val disY = abs(bossPos.y - archerPos.y)

//        Gdx.app.log("Boss Position", "x: " + bossPos.x.toString() + ", y: " + bossPos.y.toString())
//        Gdx.app.log("Archer Position", "x: " + archerPos.x + ",y: " + archerPos.y)
        if (!faceBoss) {
            if (archerPos.x == 0f || archerPos.x == V_WIDTH.toFloat()) {
                return if (bossPos.y > archerPos.y) {
                    FacingDirection.SOUTH
                } else {
                    FacingDirection.NORTH
                }
            }

            if (archerPos.y == 0f || archerPos.y == V_HEIGHT.toFloat()) {
                return if (bossPos.x > archerPos.x) {
                    FacingDirection.WEST
                } else {
                    FacingDirection.EAST
                }
            }
        }

        return if (disX > disY) {
            if (bossPos.x > archerPos.x) {
                if (faceBoss) {
                    FacingDirection.EAST
                } else {
                    FacingDirection.WEST
                }
            } else {
                if (faceBoss) {
                    FacingDirection.WEST
                } else {
                    FacingDirection.EAST
                }
            }
        } else {
            if (bossPos.y > archerPos.y) {
                if (faceBoss) {
                    FacingDirection.NORTH
                } else {
                    FacingDirection.SOUTH
                }
            } else {
                if (faceBoss) {
                    FacingDirection.SOUTH
                } else {
                    FacingDirection.NORTH
                }
            }
        }
    }
}