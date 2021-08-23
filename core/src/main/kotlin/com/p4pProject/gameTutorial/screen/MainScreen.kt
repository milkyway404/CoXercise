package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import com.p4pProject.gameTutorial.ui.SkinTextField
import com.p4pProject.gameTutorial.ui.SkinWindow
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.actors.onChange
import ktx.actors.onClick
import ktx.actors.plus
import ktx.actors.plusAssign
import ktx.async.KtxAsync
import ktx.scene2d.*

class MainScreen( game: MyGameTutorial) : GameBaseScreen(game) {

    private lateinit var invalidLobbyLabel: Label
    private lateinit var socket: Socket
    private lateinit var lobbyID: String

    override fun show() {
        Gdx.app.log("Show", "Showing")
        connectAndSetupSocket()
        setupUI()
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
                        game.addScreen(LoadingScreen(game))
                        game.removeScreen<MainScreen>()
                        dispose()
                        game.setScreen<LoadingScreen>()
                    }
                }
                row()
                textButton("Multiplayer Mode", SkinTextButton.DEFAULT.name) {
                    onClick {
                        stage.actors {
                            dialog("Lobby ID", SkinWindow.DEFAULT.name) {
                                table {
                                    textButton("Create Lobby", SkinTextButton.DEFAULT.name) {
                                        onClick {
                                            createLobby()
                                        }
                                    }
                                    row()

                                    textField("lobby id", SkinTextField.DEFAULT.name) {
                                        onChange {
                                            lobbyID = text
                                        }
                                        alignment = Align.center

                                    }
                                    row()
                                    textButton("Join Lobby", SkinTextButton.DEFAULT.name) {
                                        onClick {
                                            joinLobbyIfValid()
                                        }
                                    }
                                    row()
                                    invalidLobbyLabel = label("Lobby ID invalid", SkinLabel.DEFAULT.name) {
                                        color.a = 0f
                                    }
                                }.pad(10f)
                            }
                        }

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
        if (assets.progress.isFinished && game.containsScreen<LobbyScreen>()) {
            changeToLobbyScreen()
        }

        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun connectAndSetupSocket() {
        socket = IO.socket("http://localhost:9999")
        socket.connect()
    }

    private fun createLobby() {
        socket.emit("create lobby")
        socket.on("lobby created") { args ->
            Gdx.app.log("Lobby", "lobby created")
            lobbyID = args[0] as String
            Gdx.app.log("Lobby", "id: $lobbyID")
            addLobbyScreen()
        }
    }

    private fun addLobbyScreen() {
        if (game.containsScreen<LobbyScreen>()) {
            return
        }
        Gdx.app.log("Lobby", "adding screen")
        game.addScreen(LobbyScreen(game, lobbyID))
        Gdx.app.log("Lobby", "" + game.containsScreen<LobbyScreen>())
    }

    private fun changeToLobbyScreen() {
        game.removeScreen<MainScreen>()
        dispose()

        game.setScreen<LobbyScreen>()
    }

    private fun joinLobbyIfValid() {
        socket.emit("join room", lobbyID)
        socket.on("invalid lobby id") {
            invalidLobbyLabel.color.a = 1f
        }
        socket.on("join room successful") {
            addLobbyScreen()
        }
    }
}
