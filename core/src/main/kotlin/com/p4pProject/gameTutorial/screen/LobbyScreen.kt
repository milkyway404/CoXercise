package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.socket.emit.SocketEmit
import com.p4pProject.gameTutorial.socket.on.SocketOn
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextField
import io.socket.client.Socket
import ktx.scene2d.*

const val NUM_PLAYERS = 3;

class LobbyScreen ( game: MyGameTutorial, private val lobbyID: String, private val socket: Socket ) : GameBaseScreen(game) {

    data class Player (val socketID: String, val characterType: String);

    private var playersList: List<Player> = ArrayList();
    private var playersTextFieldList: ArrayList<TextField> = ArrayList();

    override fun show() {
        Gdx.app.log("Lobby", lobbyID)
        setupUI()
        setupSockets()
    }

    override fun render(delta: Float) {
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun setupSockets() {
        SocketOn.updatePlayers(socket, callback = { playersList -> updatePlayers(playersList)})
        SocketEmit.getLobbyPlayers(socket, lobbyID);
    }

    private fun updatePlayers(playersList: List<Player>) {
        this.playersList = playersList;
        for (i in 0 until NUM_PLAYERS) {
            if (playersList.size > i) {
                playersTextFieldList[i].text = playersList[i].characterType;
            }
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                label("Lobby", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                }
                row();
                for (i in 0 until NUM_PLAYERS) {
                    playersTextFieldList.add(textField("player yet to join...",
                            SkinTextField.DEFAULT.name));
                    row();
                }
                setFillParent(true)
                pack()
            }
            stage.isDebugAll = true
        }
    }
}