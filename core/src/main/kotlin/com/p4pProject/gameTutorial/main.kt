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
import com.p4pProject.gameTutorial.screen.GameBaseScreen
import com.p4pProject.gameTutorial.screen.MainScreen
import com.p4pProject.gameTutorial.ui.createSkin
import com.p4pProject.gameTutorial.ecs.system.animation.*
import com.p4pProject.gameTutorial.ecs.system.automation.ArcherAutomationSystem
import com.p4pProject.gameTutorial.ecs.system.automation.BossAutomationSystem
import com.p4pProject.gameTutorial.ecs.system.automation.PriestAutomationSystem
import com.p4pProject.gameTutorial.ecs.system.automation.WarriorAutomationSystem
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.debug
import ktx.log.logger
import java.util.*

private val LOG = logger<MyGameTutorial>()
const val V_WIDTH_PIXELS = 240
const val V_HEIGHT_PIXELS = 135
const val UNIT_SCALE = 1/16f
const val V_WIDTH = 16
const val V_HEIGHT = 9
const val BACKGROUND_V_WIDTH = 1462
const val BACKGROUND_V_HEIGHT = 822
class MyGameTutorial : KtxGame<GameBaseScreen>() {
    //private lateinit var socket:Socket
    val gameViewport = FitViewport(V_WIDTH.toFloat(), V_HEIGHT.toFloat())
    val uiViewport = FitViewport(V_WIDTH_PIXELS.toFloat(), V_HEIGHT_PIXELS.toFloat());
    val backgroundViewport = FitViewport(BACKGROUND_V_WIDTH.toFloat(), BACKGROUND_V_HEIGHT.toFloat());
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
        val warriorGraphicAtlas = assets[TextureAtlasAsset.WARRIOR_GRAPHICS.descriptor]
        val archerGraphicAtlas = assets[TextureAtlasAsset.ARCHER_GRAPHICS.descriptor]
        val priestGraphicAtlas = assets[TextureAtlasAsset.PRIEST_GRAPHICS.descriptor]
        val bossGraphicAtlas = assets[TextureAtlasAsset.BOSS_GRAPHICS.descriptor]

        addSystem(PlayerInputSystem(gameViewport, gameEventManager))
        addSystem(MoveSystem(gameEventManager))
        addSystem(PowerUpSystem(gameEventManager, audioService))
        addSystem(DamageSystem(gameEventManager))
        addSystem(PlayerDamageSystem(gameEventManager))
        addSystem(CameraShakeSystem(gameViewport.camera, gameEventManager))
        addSystem(
            WarriorAnimationSystem(
                warriorGraphicAtlas, gameEventManager
            )
        )
        addSystem(
            PriestAnimationSystem(
                priestGraphicAtlas,
                gameEventManager
        )
        )
        addSystem(BossAnimationSystem(
            bossGraphicAtlas,
            gameEventManager
        ))
        addSystem(ArcherAnimationSystem(archerGraphicAtlas, gameEventManager))
        addSystem(AttachSystem())
        addSystem(HealSystem(gameEventManager))
        addSystem(AnimationSystem(graphicsAtlas))
        addSystem(RenderSystem(
            batch,
            gameViewport,
            uiViewport,
            backgroundViewport,
            assets[TextureAsset.BACKGROUND.descriptor],
            gameEventManager,
        assets[ShaderProgramAsset.OUTLINE.descriptor]))
        addSystem(RemoveSystem())
        addSystem(DebugSystem())
        }
    }

    override fun create() {
        //connectSocket()
        //configSocketEvents()
        Gdx.app.logLevel = 3
        LOG.debug { "Create game instance" }

        val assetRefs = gdxArrayOf(
            TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assets.loadAsync(it.descriptor) },
            BitmapFontAsset.values().map{ assets.loadAsync(it.descriptor)}
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            createSkin(assets)
            addScreen(MainScreen(this@MyGameTutorial))
            setScreen<MainScreen>()

        }
    }

    override fun dispose() {
        super.dispose()
        LOG.debug { "Sprites in batch : ${(batch as SpriteBatch).maxSpritesInBatch}" }
        batch.dispose()
        assets.dispose()
        stage.dispose()
        //socket.disconnect()
    }

//    private fun connectSocket() {
//        // TODO change this URL if hosting
//        socket = IO.socket("http://localhost:9999")
//        socket.connect()
//    }
//
//    private fun configSocketEvents() {
//        socket.on(Socket.EVENT_CONNECT) {
//            Gdx.app.log("SocketIO", "connected")
//        }.on("socketID") { args ->
//            val data = args[0] as JSONObject
//            Gdx.app.log("SocketIO", "My ID: ${data.getString("id")}")
//        }.on("newPlayer") { args ->
//            val data = args[0] as JSONObject
//            Gdx.app.log("SocketIO", "New Player ID: ${data.getString("id")}")
//        }
//    }
}