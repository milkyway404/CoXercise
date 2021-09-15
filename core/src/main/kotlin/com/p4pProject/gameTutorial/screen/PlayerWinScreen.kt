package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.socket.emit.SocketEmit
import com.p4pProject.gameTutorial.socket.on.SocketOn
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import com.p4pProject.gameTutorial.ui.SkinTextField
import com.p4pProject.gameTutorial.ui.SkinWindow
import io.socket.client.IO
import io.socket.client.Socket
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.scene2d.*
import org.json.JSONObject


class PlayerWinScreen( game: MyGameTutorial) : GameBaseScreen(game) {

    override fun show() {
        setupUI()
    }

    private fun setupUI() {
        // TODO design main page one day
        stage.actors {
            table {
                defaults().fillX().expandX()
                label("CONGRATULATIONS", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                    wrap = true
                    color.set(Color.WHITE)
                }
                row()
                label("YOU WIN!!", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                    wrap = true
                    color.set(Color.WHITE)
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


}
