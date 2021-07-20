package com.p4pProject.gameTutorial

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.p4pProject.gameTutorial.audio.AudioService
import com.p4pProject.gameTutorial.audio.DefaultAudioService
import com.p4pProject.gameTutorial.ecs.asset.BitmapFontAsset
import com.p4pProject.gameTutorial.ecs.asset.ShaderProgramAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAtlasAsset
import com.p4pProject.gameTutorial.ecs.system.*
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.GameTutorialScreen
import com.p4pProject.gameTutorial.screen.LoadingScreen
import com.p4pProject.gameTutorial.ui.createSkin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<MyGameTutorial>()
const val V_WIDTH_PIXELS = 135
const val V_HEIGHT_PIXELS = 240
const val UNIT_SCALE = 1/16f
const val V_WIDTH = 9
const val V_HEIGHT = 16
class MyGameTutorial : KtxGame<GameTutorialScreen>() {
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val uiViewport = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat());
    val stage: Stage by lazy {
        val result = Stage(uiViewport, batch)
        Gdx.input.inputProcessor = result
        result
    }
    val batch: Batch by lazy { SpriteBatch() }
    val gameEventManager = GameEventManager()
    val assets : AssetStorage by lazy {
        KtxAsync.initiate()
        AssetStorage()
    }

    val audioService : AudioService by lazy {
        DefaultAudioService(assets)
    }

    val preferences : Preferences by lazy {
        Gdx.app.getPreferences("game-tutorial")
    }

    val engine: Engine by lazy { PooledEngine().apply {

        val graphicsAtlas = assets[TextureAtlasAsset.GAME_GRAPHICS.descriptor]

        addSystem(PlayerInputSystem(gameViewport))
        addSystem(MoveSystem())
        addSystem(PowerUpSystem(gameEventManager, audioService))
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
            gameEventManager,
        assets[ShaderProgramAsset.OUTLINE.descriptor]))
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        Gdx.app.logLevel = 3
        LOG.debug { "Create game instance" }

        val assetRefs = gdxArrayOf(
            TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assets.loadAsync(it.descriptor) },
            BitmapFontAsset.values().map{ assets.loadAsync(it.descriptor)}
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            createSkin(assets)
            addScreen(LoadingScreen(this@MyGameTutorial))
            setScreen<LoadingScreen>()

        }
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch : ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
        assets.dispose()
        stage.dispose()
    }
}