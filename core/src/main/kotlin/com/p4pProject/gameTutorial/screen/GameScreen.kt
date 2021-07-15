package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.viewport.FitViewport
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.UNIT_SCALE
import com.p4pProject.gameTutorial.V_WIDTH
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.ecs.system.DAMAGE_AREA_HEIGHT
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger
import kotlin.math.min


private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(game: MyGameTutorial): GameTutorialScreen(game) {

    private  val player = engine.entity{
        with<TransformComponent>{
            setInitialPosition(3f,9f,-1f)
        }
        with<MoveComponent>()
        with<GraphicComponent>()
        with<PlayerComponent>()
        with<FacingComponent>()
    }

    private val fireAnimation = engine.entity {
        with<TransformComponent>()
        with<AttachComponent>{
            entity = player
            offset.set(1f * UNIT_SCALE, -6f * UNIT_SCALE)
        }
        with<GraphicComponent>()
        with<AnimationComponent> {
            type = AnimationType.FIRE
        }
    }

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


    override fun show() {
        LOG.debug{ "Game screen is shown" }
    }


    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))

    }
}