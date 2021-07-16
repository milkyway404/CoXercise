package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.MyGameTutorial
import ktx.app.KtxScreen

abstract class GameTutorialScreen (
    val game: MyGameTutorial,
    val batch: Batch = game.batch,
    val gameViewport: Viewport = game.gameViewport,
    val uiViewport: Viewport =game.uiViewport,
    val engine: Engine = game.engine): KtxScreen{

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width,height, true)
        uiViewport.update(width,height, true)
    }
}