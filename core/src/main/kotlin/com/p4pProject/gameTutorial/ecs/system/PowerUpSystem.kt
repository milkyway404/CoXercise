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
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.*
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import kotlin.math.min
import kotlin.random.Random


private val LOG = logger<PowerUpSystem>()
private const val MAX_SPAWN_INTERVAL = 10f
private const val MIN_SPAWN_INTERVAL = 3f
private const val MIN_POWER_UP_DURATION = 5f
private const val MAX_POWER_UP_DURATION = 20f

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

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        spawnTime -= deltaTime
        if (spawnTime<=0f){
            spawnTime = MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL)

            spawnPowerUp(PowerUpType.MP_GAIN, 1f * MathUtils.random(1, V_WIDTH - 1),
                1f * MathUtils.random(1, V_HEIGHT - 1))
        }
    }

    private fun spawnPowerUp(powerUpType: PowerUpType, x: Float, y: Float) {
        engine.entity{
            with<TransformComponent>{
                setInitialPosition(x,y,0f)
            }
            with<PowerUpComponent> { type = powerUpType; duration = getRandomDuration() }
            with<AnimationComponent> { type = powerUpType.animationType }
            with<GraphicComponent>()
            with<MoveComponent>()
        }
    }

    private fun getRandomDuration(): Float { return Random.nextFloat() *
            (MAX_POWER_UP_DURATION- MIN_POWER_UP_DURATION) + MIN_POWER_UP_DURATION }

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
        if(chosenCharacterType != player[PlayerComponent.mapper]?.characterType) return

        val powerUpCmp = powerUp[PowerUpComponent.mapper]
        require(powerUpCmp != null ){"Entity |entity| must have a PowerUpComponent. entity=$powerUp"}

        powerUpCmp.type.also { powerUpType ->

            LOG.debug { "Picking up power up of type $powerUpType" }

            player[PlayerComponent.mapper]?.let {
                it.mp = min(it.maxMp, it.mp + powerUpType.mpGain)
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