package com.p4pProject.gameTutorial.socket.on

import com.badlogic.gdx.Gdx
import com.p4pProject.gameTutorial.screen.LobbyScreen
import io.socket.client.Socket
import org.json.JSONArray

class SocketOn(private val socket: Socket) {
    fun setupSockets() {
        lobbyCreated(socket);
        invalidLobbyID(socket);
        characterTaken(socket);
    }

    companion object {
        fun lobbyCreated(socket: Socket, callback: ((String) -> Unit)? = null) {
            socket.on("lobby created") { args ->
                Gdx.app.log("Lobby", "lobby created")
                if (callback != null) {
                    val lobbyID = args[0] as String
                    Gdx.app.log("Lobby", "id: $lobbyID")

                    callback(lobbyID);
                };
            }
        }

        fun invalidLobbyID(socket: Socket, invalidLobbyID: (() -> Unit)? = null) {
            socket.on("invalid lobby id") {
                if (invalidLobbyID != null) {
                    invalidLobbyID()
                };
            }
        }

        fun characterTaken(socket: Socket, characterTaken: (() -> Unit)? = null) {
            socket.on("characterTaken") {
                if (characterTaken != null) {
                    characterTaken()
                };
            }
        }

        fun joinLobbySuccessful(socket: Socket, callback: (() -> Unit)?) {
            socket.on("join lobby successful") {
                if (callback != null) {
                    callback()
                }
            }
        }

        fun updatePlayers(socket: Socket, callback: ((List<LobbyScreen.Player>) -> Unit)?) {
            socket.on("update players") { args ->
                Gdx.app.log("Socket", "update players");
                val players = args[0] as JSONArray
                val playersList = ArrayList<LobbyScreen.Player>()
                for ( i in 0 until players.length() ) {
                    val player = players.getJSONObject(i);
                    val playerCharacterType = player.getString("characterType");
                    val playerSocketID = player.getString("socketID");
                    playersList.add(LobbyScreen.Player(playerSocketID, playerCharacterType))
                }
                if (callback != null) {
                    callback(playersList)
                };
            }
        }
    }


}