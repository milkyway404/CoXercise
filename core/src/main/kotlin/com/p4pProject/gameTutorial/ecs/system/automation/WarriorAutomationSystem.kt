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
import com.p4pProject.gameTutorial.socket.emit.SocketEmit
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.abs

const val WARRIOR_MOVEMENT_SPEED = 0.25f

class WarriorAutomationSystem(
    private val gameEventManager: GameEventManager): IteratingSystem(allOf(PlayerComponent::class,
    TransformComponent::class, FacingComponent::class, WarriorAnimationComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = entity[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$entity" }
        Gdx.app.log("meow", "processing warrior automation system")

        if (gameMode == GameMode.MULTIPLAYER || player.characterType != CharacterType.WARRIOR ||
                chosenCharacterType == CharacterType.WARRIOR) {
            Gdx.app.log("gameMode, character type, chosen character", gameMode.toString() + player.characterType + chosenCharacterType)
            return
        }

        walkToAndAttackBoss(entity)
    }

    private fun walkToAndAttackBoss(warrior: Entity) {

        Gdx.app.log("Walk to or run away or attack", "warrior")

        val player = warrior[PlayerComponent.mapper]
        require(player != null) { "Entity |entity| must have a PlayerComponent. entity=$warrior" }

        val facing = warrior[FacingComponent.mapper]
        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$facing" }

        if (player.isAttacking || player.isSpecialAttacking) {
            Gdx.app.log("Warrior Automation is attacking", player.isAttacking.toString() + player.isSpecialAttacking)
            return
        }

        val bossInfo = warrior[BossInfoComponent.mapper]
        if (bossInfo == null || !bossInfo.bossIsInitialized()) {
            Gdx.app.log("Warrior Automation boss not initialised", bossInfo.toString() + bossInfo?.bossIsInitialized())
            return
        }

        val bossTrans = bossInfo.boss[TransformComponent.mapper]!!
        val warriorTrans = warrior[TransformComponent.mapper]!!

        facing.direction = getDirectionBossAt(warriorTrans, bossTrans)

        if (isBossInAttackRange(warriorTrans, bossTrans)) {
            // attack
            Gdx.app.log("Attack", "warrior")
            // TODO emit socket player attack
//            SocketEmit.playerAttack(socket, lobbyID, CharacterType.WARRIOR.name)
            attackBoss(warrior)
        } else {
            // walk
            Gdx.app.log("Walk", "warrior")
            walkToBoss(warriorTrans, facing.direction)
        }
    }

    private fun attackBoss(warrior: Entity) {
        val player = warrior[PlayerComponent.mapper]!!

        if (player.mp > player.specialAttackMpCost) {
            player.mp -= player.specialAttackMpCost
            gameEventManager.dispatchEvent(GameEvent.WarriorSpecialAttackEvent.apply {
                this.player = warrior
            })
        } else {
            gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                this.player = warrior
            })
        }
    }

    private fun walkToBoss(warriorTrans: TransformComponent, facingDirection: FacingDirection) {
        val warriorPos = warriorTrans.position

        when (facingDirection) {
            FacingDirection.NORTH -> {
                warriorPos.y = MathUtils.clamp(warriorPos.y + WARRIOR_MOVEMENT_SPEED, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.SOUTH -> {
                warriorPos.y = MathUtils.clamp(warriorPos.y - WARRIOR_MOVEMENT_SPEED, 0f, V_HEIGHT.toFloat())
            }
            FacingDirection.EAST -> {
                warriorPos.x = MathUtils.clamp(warriorPos.x + WARRIOR_MOVEMENT_SPEED, 0f, V_WIDTH - warriorTrans.size.x)
            }
            FacingDirection.WEST -> {
                warriorPos.x = MathUtils.clamp(warriorPos.x - WARRIOR_MOVEMENT_SPEED, 0f, V_WIDTH - warriorTrans.size.x)
            }
        }
    }

    private fun isBossInAttackRange(warriorTrans: TransformComponent, bossTrans: TransformComponent): Boolean {
        return warriorTrans.overlaps(bossTrans)
    }

    private fun getDirectionBossAt(warriorTrans: TransformComponent, bossTrans: TransformComponent): FacingDirection {
        val bossPos = bossTrans.position
        val warriorPos = warriorTrans.position

        // go in the direction of the longest distance
        val disX = abs(bossPos.x - warriorPos.x)
        val disY = abs(bossPos.y - warriorPos.y)

//        Gdx.app.log("Boss Position", "x: " + bossPos.x.toString() + ", y: " + bossPos.y.toString())
//        Gdx.app.log("Warrior Position", "x: " + warriorPos.x + ",y: " + warriorPos.y)

        return if (disX > disY) {
            if (bossPos.x > warriorPos.x) {
                FacingDirection.EAST
            } else {
                FacingDirection.WEST
            }
        } else {
            if (bossPos.y > warriorPos.y) {
                FacingDirection.NORTH
            } else {
                FacingDirection.SOUTH
            }
        }
    }

}