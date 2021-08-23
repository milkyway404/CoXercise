package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.async.KtxAsync
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class MainScreen( game: MyGameTutorial) : GameBaseScreen(game) {

    override fun show() {
        setupUI();
        KtxAsync.launch {
            assetsLoaded()
        }
    }

    private fun setupUI() {
        // TODO design main page one day
        stage.actors {
            table {
                defaults().fillX().expandX()
                label("ExerQuest", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                    wrap = true
                    color.set(Color.WHITE)
                }
                row()
                label("CoXercise", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                    wrap = true
                    color.set(Color.WHITE)
                }
                row()
                textButton("Singleplayer Mode", SkinTextButton.DEFAULT.name) {
                    onClick {
                        game.removeScreen<MainScreen>()
                        dispose()
                        game.setScreen<LoadingScreen>()
                    }
                }
                row()
                textButton("Multiplayer Mode", SkinTextButton.DEFAULT.name) {
                    onClick {
                        game.removeScreen<MainScreen>()
                        dispose()
                        game.setScreen<LobbyScreen>()
                    }
                }

                setFillParent(true)
                pack()
            }
            // TODO remove in production
            stage.isDebugAll = true
        }
    }

    override fun hide() {
        stage.clear()
    }

    override fun render(delta: Float) {
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun assetsLoaded() {
        game.addScreen(LobbyScreen(game))
        game.addScreen(LoadingScreen(game))
    }
}
