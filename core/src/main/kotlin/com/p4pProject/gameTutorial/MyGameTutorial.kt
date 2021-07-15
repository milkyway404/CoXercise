package com.p4pProject.gameTutorial

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.p4pProject.gameTutorial.ecs.system.*
import com.p4pProject.gameTutorial.screen.GameScreen
import com.p4pProject.gameTutorial.screen.GameTutorialScreen
import ktx.app.KtxGame
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<MyGameTutorial>()
const val UNIT_SCALE = 1/16f
const val V_WIDTH = 16
const val V_HEIGHT = 9
class MyGameTutorial : KtxGame<GameTutorialScreen>() {

    val gameViewport = FitViewport(9f, 16f)
    val batch: Batch by lazy { SpriteBatch() }


    val graphicsAtlas by lazy { TextureAtlas(Gdx.files.internal("graphics/graphics.atlas"))}


    val engine: Engine by lazy { PooledEngine().apply {
        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(DamageSystem())
        addSystem(
            PlayerAnimationSystem(
                graphicsAtlas.findRegion("ship_base"),
                graphicsAtlas.findRegion("ship_left"),
                graphicsAtlas.findRegion("ship_right")
            )
        )
        addSystem(AttachSystem())
        addSystem(AnimationSystem(graphicsAtlas))
        addSystem(RenderSystem(batch, gameViewport))
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = 3
        LOG.debug { "Create game instance" }
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch : ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()

        graphicsAtlas.dispose()

    }
}