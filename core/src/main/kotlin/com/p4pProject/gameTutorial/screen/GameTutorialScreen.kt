package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.audio.AudioService
import com.p4pProject.gameTutorial.event.GameEventManager
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage

abstract class GameTutorialScreen (
    val game: MyGameTutorial,
    val gameViewport: Viewport = game.gameViewport,
    val uiViewport: Viewport =game.uiViewport,
    val gameEventManager: GameEventManager = game.gameEventManager,
    val assets : AssetStorage = game.assets,
    val audioService: AudioService = game.audioService
): KtxScreen{

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width,height, true)
        uiViewport.update(width,height, true)
    }
}