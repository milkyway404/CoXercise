package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.UNIT_SCALE
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.ecs.asset.MusicAsset
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.ecs.system.DAMAGE_AREA_HEIGHT
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import kotlin.math.min


private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: MyGameTutorial,
    private val engine: Engine = game.engine
): GameTutorialScreen(game), GameEventListener {

    private val background = engine.entity{
        with<TransformComponent>{
            size.set(
                V_WIDTH.toFloat(),
                DAMAGE_AREA_HEIGHT
            )
        }
        with<AnimationComponent>{
            type = AnimationType.DARK_MATTER
        }
        with<GraphicComponent>()
    }


    private fun spawnPlayer (){
        val player = engine.entity{
            with<TransformComponent>{
                setInitialPosition(3f,9f,-1f)
                setSize(18f * UNIT_SCALE, 18f * UNIT_SCALE)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }

        engine.entity {
            with<TransformComponent>()
            with<AttachComponent> {
                entity = player
                offset.set(1f * UNIT_SCALE, -6f * UNIT_SCALE)
            }
            with<GraphicComponent>()
            with<AnimationComponent> {
                type = AnimationType.FIRE
            }
        }
    }

    override fun show() {
        LOG.debug{ "Game screen is shown" }
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)

        audioService.play(MusicAsset.GAME)
        spawnPlayer ()

    }

    override fun hide() {
        super.hide()
        gameEventManager.removeListener(GameEvent.PlayerDeath::class, this)
    }


    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
        audioService.update()
    }

    override fun onEvent(event: GameEvent) {
        when (event){
            is GameEvent.PlayerDeath -> {
                spawnPlayer()
            }
            GameEvent.CollectPowerUp -> TODO()
        }
    }
}