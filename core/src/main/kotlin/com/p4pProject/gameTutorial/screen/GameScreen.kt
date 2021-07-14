package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.viewport.FitViewport
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.UNIT_SCALE
import com.p4pProject.gameTutorial.ecs.component.FacingComponent
import com.p4pProject.gameTutorial.ecs.component.GraphicComponent
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger


private val LOG = logger<MyGameTutorial>()

class GameScreen(game: MyGameTutorial): GameTutorialScreen(game) {

    private  val player = engine.entity{
        with<TransformComponent>{
            position.set(3f,9f,0f)
        }
        with<GraphicComponent>()
        with<PlayerComponent>()
        with<FacingComponent>()
    }


    override fun show() {
        LOG.debug{ "Game screen is shown" }
    }


    override fun render(delta: Float) {
        engine.update(delta)

    }
}