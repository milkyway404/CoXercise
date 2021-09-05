package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ecs.asset.ShaderProgramAsset
import com.p4pProject.gameTutorial.ecs.asset.SoundAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAtlasAsset
import com.p4pProject.gameTutorial.ui.SkinLabel
import io.socket.client.Socket
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.scene2d.*

class LoadingScreen(
    game: MyGameTutorial,
    private val socket: Socket,
    private val lobbyID: String,
    private val chosenCharacterType: CharacterType) : GameBaseScreen(game){

    private lateinit var progressBar : Image
    private lateinit var touchToBeginLabel: Label

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

        setupUI()
    }

    override fun hide() {
        stage.clear()
    }

    private fun setupUI() {
        stage.actors {
            table {
                defaults().fillX().expandX()
                label("Loading Screen", SkinLabel.LARGE.name){
                    wrap = true
                    setAlignment(Align.center)
                }
                row()
                touchToBeginLabel = label("Touch To Begin",SkinLabel.LARGE.name){
                    wrap = true
                    setAlignment(Align.center)
                    color.a = 0f
                }
                row()
                stack{ cell ->
                    progressBar = image("life_bar").apply {
                        scaleX = 0f
                    }
                    label("Loading...", SkinLabel.LARGE.name){
                        setAlignment(Align.center)
                    }
                    cell.padLeft(5f).padRight(5f)
                }
                setFillParent(true)
                pack()
            }
        }
        // allows you to see the borders of components on screen
        stage.isDebugAll = true
    }

    override fun render(delta: Float) {
        if(assets.progress.isFinished && Gdx.input.justTouched() && game.containsScreen<GameScreen>()){
            game.removeScreen<LoadingScreen>()
            dispose()
            game.setScreen<GameScreen>()
        }
        progressBar.scaleX = assets.progress.percent
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }
    private fun assetsLoaded() {
        game.addScreen(GameScreen(game, socket = socket, lobbyID = lobbyID, chosenCharacterType = chosenCharacterType))
        touchToBeginLabel += Actions.forever(sequence(Actions.fadeIn(0.5f) + fadeOut(0.5f)))
    }
}