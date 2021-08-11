package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.p4pProject.gameTutorial.V_HEIGHT
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.audio.AudioService
import com.p4pProject.gameTutorial.ecs.asset.SoundAsset
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventManager
import ktx.ashley.*
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import kotlin.math.min
import kotlin.random.Random


private val LOG = logger<PowerUpSystem>()
private const val MAX_SPAWN_INTERVAL = 1.5f
private const val MIN_SPAWN_INTERVAL = 0.9f
private const val POWER_UP_SPEED = -8.75f

private class SpawnPattern(
    type1:PowerUpType = PowerUpType.NONE,
    type2:PowerUpType = PowerUpType.NONE,
    type3:PowerUpType = PowerUpType.NONE,
    type4:PowerUpType = PowerUpType.NONE,
    type5:PowerUpType = PowerUpType.NONE,
    val types : GdxArray<PowerUpType> = gdxArrayOf(type1, type2, type3, type4, type5)

)

class PowerUpSystem (
    private val gameEventManager: GameEventManager,
    private val audioService: AudioService
        ) : IteratingSystem (
    allOf(PowerUpComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get()){

    private val playerBoundingRect = Rectangle()
    private val powerBoundingRect = Rectangle()

    private val playerEntities by lazy {
        engine.getEntitiesFor(
            allOf(PlayerComponent::class).exclude(RemoveComponent::class).get()
        )
    }

    private var spawnTime = 0f
    private val spawnPatterns = gdxArrayOf(
        SpawnPattern(type1 = PowerUpType.SPEED_1, type2 = PowerUpType.SPEED_2, type5 = PowerUpType.LIFE),
        SpawnPattern(type2 = PowerUpType.LIFE, type3 = PowerUpType.SHIELD, type4 = PowerUpType.SPEED_2)
    )

    private val currentSpawnPattern = GdxArray<PowerUpType>()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        spawnTime -= deltaTime
        if (spawnTime<=0f){
            spawnTime = MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)

            if (currentSpawnPattern.isEmpty){
                currentSpawnPattern.addAll(spawnPatterns[MathUtils.random(0, spawnPatterns.size -1)].types)
                LOG.debug { "Next pattern: $currentSpawnPattern" }
            }

            val powerUpType = currentSpawnPattern.removeIndex(0)
            if(powerUpType == PowerUpType.NONE){
                return
            }

            spawnPowerUp(powerUpType, 1f * MathUtils.random(1, V_WIDTH - 1),
                1f * MathUtils.random(1, V_HEIGHT - 1))
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType, x: Float, y: Float) {
        engine.entity{
            with<TransformComponent>{
                setInitialPosition(x,y,0f)
                LOG.debug { "Spawn power of type $powerUpType at $position" }
            }
            with<PowerUpComponent> { type = powerUpType; duration = getRandomDuration() }
            with<AnimationComponent> { type = powerUpType.animationType }
            with<GraphicComponent>()
            with<MoveComponent>()
        }
    }

    private fun getRandomDuration(): Float { return Random.nextFloat() * 30 }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        val powerUp = entity[PowerUpComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        require(powerUp != null ){"Entity |entity| must have a PowerUpComponent. entity=$entity"}

        powerUp.duration -= deltaTime

        if(powerUp.duration <= 0){
            entity.addComponent<RemoveComponent>(engine)
            return
        }

        playerEntities.forEach { player ->
            player[TransformComponent.mapper]?.let { playerTransform->
                playerBoundingRect.set(
                    playerTransform.position.x,
                    playerTransform.position.y,
                    playerTransform.size.x,
                    playerTransform.size.y
                )

                powerBoundingRect.set(
                    transform.position.x,
                    transform.position.y,
                    transform.size.x,
                    transform.size.y
                )

                if (playerBoundingRect.overlaps(powerBoundingRect)){
                    collectPowerUp(player, entity)
                }
            }
        }
    }

    private fun collectPowerUp(player: Entity, powerUp: Entity) {
        val powerUpCmp = powerUp[PowerUpComponent.mapper]
        require(powerUpCmp != null ){"Entity |entity| must have a PowerUpComponent. entity=$powerUp"}

        powerUpCmp.type.also { powerUpType ->

            LOG.debug { "Picking up power up of type $powerUpType" }

            player[MoveComponent.mapper]?.let { it.speed.y += powerUpType.speedGain }
            player[PlayerComponent.mapper]?.let {
                it.hp = min(it.maxHp, it.hp + powerUpType.lifeGain)
                //it.shield = min(it.maxShield, it.shield + powerUpType.speedGain)
            }
            audioService.play(powerUpType.soundAsset)
        gameEventManager.dispatchEvent(
            GameEvent.CollectPowerUp.apply {
                this.player = player
                this.type = powerUpCmp.type })
            }
        powerUp.addComponent<RemoveComponent>(engine)
    }
    fun reset() {
        spawnTime = 0f
        entities.forEach {
            it.addComponent<RemoveComponent>(engine)
        }
    }
}