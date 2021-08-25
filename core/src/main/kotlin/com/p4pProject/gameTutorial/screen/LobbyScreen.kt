package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import io.socket.client.Socket
import ktx.scene2d.*
import org.json.JSONArray
import org.json.JSONObject

class LobbyScreen ( game: MyGameTutorial, private val lobbyID: String, private val socket: Socket ) : GameBaseScreen(game) {

    private data class Player (val socketID: String, val characterType: String);

    private val playersList = ArrayList<Player>()

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
        socket.on("update players") { args ->
            Gdx.app.log("Socket", "updating players");
            val players = args[0] as JSONArray
            Gdx.app.log("Socket", players.toString());
            for ( i in 0 until players.length() ) {
                val player = players.getJSONObject(i);
                val playerCharacterType = player.getString("characterType");
                val playerSocketID = player.getString("socketID");
                playersList.add(Player(playerSocketID, playerCharacterType))
            }
            updatePlayers();
        }

        socket.emit("joined lobby", lobbyID)
    }

    private fun updatePlayers() {
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