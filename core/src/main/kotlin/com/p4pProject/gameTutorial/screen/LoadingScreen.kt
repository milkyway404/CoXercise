package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.graphics.Texture
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ecs.asset.ShaderProgramAsset
import com.p4pProject.gameTutorial.ecs.asset.SoundAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAtlasAsset
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf

class LoadingScreen(game: MyGameTutorial) : GameTutorialScreen(game){
    override fun show() {
        // queue assets loading
        val assetRefs = gdxArrayOf(
            TextureAsset.values().map {
                assets.loadAsync(it.descriptor)
            } ,

            TextureAtlasAsset.values().map {
                assets.loadAsync(it.descriptor)
            } ,

            SoundAsset.values().map {
                assets.loadAsync(it.descriptor)
            } ,

            ShaderProgramAsset.values().map {
                assets.loadAsync(it.descriptor)
            }
        ).flatten()
        // once assets are loaded -> change to GameScreen
        KtxAsync.launch {
            assetRefs.joinAll()
            assetsLoaded()
        }
    }

    private fun assetsLoaded() {
        game.addScreen(GameScreen(game))
        game.setScreen<GameScreen>()
        game.removeScreen<LoadingScreen>()
        dispose()
    }
}