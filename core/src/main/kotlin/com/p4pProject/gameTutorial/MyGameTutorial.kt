package com.p4pProject.gameTutorial

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.viewport.FitViewport
import com.p4pProject.gameTutorial.ecs.asset.TextureAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAtlasAsset
import com.p4pProject.gameTutorial.ecs.system.*
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.GameScreen
import com.p4pProject.gameTutorial.screen.GameTutorialScreen
import com.p4pProject.gameTutorial.screen.LoadingScreen
import ktx.app.KtxGame
import ktx.assets.async.Asset
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<MyGameTutorial>()
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240
const val UNIT_SCALE = 1/16f
const val V_WIDTH = 9
const val V_HEIGHT = 16
class MyGameTutorial : KtxGame<GameTutorialScreen>() {

    val gameViewport = FitViewport(9f, 16f)
    val uiViewport = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat());
    val batch: Batch by lazy { SpriteBatch() }
    val gameEventManager = GameEventManager()
    val assets : AssetStorage by lazy {
        KtxAsync.initiate()
        AssetStorage()
    }

    val engine: Engine by lazy { PooledEngine().apply {

        val graphicsAtlas = assets[TextureAtlasAsset.GAME_GRAPHICS.descriptor]

        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(PowerUpSystem(gameEventManager))
        addSystem(DamageSystem(gameEventManager))
        addSystem(CameraShakeSystem(gameViewport.camera, gameEventManager))
        addSystem(
            PlayerAnimationSystem(
                graphicsAtlas.findRegion("ship_base"),
                graphicsAtlas.findRegion("ship_left"),
                graphicsAtlas.findRegion("ship_right")
            )
        )
        addSystem(AttachSystem())
        addSystem(AnimationSystem(graphicsAtlas))
        addSystem(RenderSystem(
            batch,
            gameViewport,
            uiViewport,
            assets[TextureAsset.BACKGROUND.descriptor],
            gameEventManager))
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = 3
        LOG.debug { "Create game instance" }
        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch : ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
        assets.dispose()
    }
}