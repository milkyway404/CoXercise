package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.socket.on.SocketOn
import com.p4pProject.gameTutorial.ui.SkinLabel
import io.socket.client.Socket
import ktx.scene2d.*
import org.json.JSONArray

class LobbyScreen ( game: MyGameTutorial, private val lobbyID: String, private val socket: Socket ) : GameBaseScreen(game) {

    data class Player (val socketID: String, val characterType: String);

    private var playersList = ArrayList<Player>()

    init {
        setupSockets()
    }

    override fun show() {
        Gdx.app.log("Lobby", lobbyID)
        setupSockets()
        setupUI()
    }

    override fun render(delta: Float) {
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun setupSockets() {
        SocketOn.updatePlayers(socket, updatePlayers = { updatePlayers(playersList)})
    }

    private fun updatePlayers(playersList: ArrayList<Player>) {
        Gdx.app.log("Socket", "players before update");
        this.playersList = playersList;
        Gdx.app.log("Socket", "players after update");
        for (player in playersList) {
            stage.actors {
                textField(player.characterType)
            }
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                label("Lobby", SkinLabel.LARGE.name) {
                    setAlignment(Align.center)
                }
                setFillParent(true)
                pack()
            }
            stage.isDebugAll = true
        }
    }
}