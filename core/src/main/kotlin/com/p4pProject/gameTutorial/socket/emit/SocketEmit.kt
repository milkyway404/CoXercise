package com.p4pProject.gameTutorial.socket.emit

import io.socket.client.Socket
import org.json.JSONObject

class SocketEmit() {

    companion object {
        fun createLobby(socket: Socket, chosenCharacterType: String) {
            socket.emit("create lobby", chosenCharacterType)
        }

        fun joinLobby(socket: Socket, data: JSONObject) {
            socket.emit("join lobby", data)
        }
    }
}