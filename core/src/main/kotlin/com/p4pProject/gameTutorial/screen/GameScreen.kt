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
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import kotlin.math.min


private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: MyGameTutorial,
    private val engine: Engine = game.engine
): GameTutorialScreen(game), GameEventListener {


    private fun spawnPlayer (){
        val player = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,3f,-1f)
                setSize(50f * UNIT_SCALE, 50f * UNIT_SCALE)
            }
            with<PlayerAnimationComponent>()
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
        LOG.debug { "${preferences["highscore", 0f]}" }
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)

        audioService.play(MusicAsset.GAME)
        spawnPlayer ()

        val background = engine.entity{
            with<TransformComponent>()
            with<GraphicComponent> {
                isBackground()
            }
        }

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
                LOG.debug { "Player died with a distance of ${event.distance}" }
                preferences.flush {
                    this["highscore"] = event.distance
                }
                spawnPlayer()
            }
            GameEvent.CollectPowerUp -> TODO()
        }
    }
}