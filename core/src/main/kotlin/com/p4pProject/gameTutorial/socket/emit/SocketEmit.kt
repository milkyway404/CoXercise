package com.p4pProject.gameTutorial.socket.emit

import io.socket.client.Socket
import org.json.JSONObject

class SocketEmit {

    companion object {
        fun createLobby(socket: Socket, chosenCharacterType: String) {
            socket.emit("create lobby", chosenCharacterType)
        }

        fun joinLobby(socket: Socket, data: JSONObject) {
            socket.emit("join lobby", data)
        }

        fun getLobbyPlayers(socket: Socket, lobbyID: String) {
            socket.emit("get lobby players", lobbyID)
        }

        fun playerMove(socket: Socket, lobbyID: String, x: Float, y: Float) {
            val data = JSONObject()
            data.put("lobbyID", lobbyID)
            data.put("x", x)
            data.put("y", y)
            socket.emit("player move", data)
        }

        fun startGame(socket: Socket, lobbyID: String) {
            socket.emit("start game", lobbyID)
        }

        fun playerAttack(socket: Socket, lobbyID: String, characterType: String){
            val data = JSONObject()
            data.put("lobbyID", lobbyID)
            data.put("characterType", characterType)
            socket.emit("player attack", data)
        }

        fun playerSpecialAttack(socket: Socket, lobbyID: String, characterType: String){
            val data = JSONObject()
            data.put("lobbyID", lobbyID)
            data.put("characterType", characterType)
            socket.emit("player special attack", data)
        }
    }
}