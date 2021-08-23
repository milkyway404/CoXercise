package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import ktx.scene2d.*

class LobbyScreen ( game: MyGameTutorial ) : GameBaseScreen(game) {

    override fun show() {
        setupUI()
    }

    override fun render(delta: Float) {
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                label("Lobby", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                }
                row()

                setFillParent(true)
                pack()
            }
            stage.isDebugAll = true
        }
    }
}