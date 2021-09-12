package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
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

lateinit var chosenCharacterType: CharacterType;

class MainScreen( game: MyGameTutorial) : GameBaseScreen(game) {

    private lateinit var invalidLobbyLabel: Label
    private lateinit var socket: Socket
    private var lobbyID: String = ""

    init {
        connectAndSetupSocket()
    }
    override fun show() {
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
                        stage.actors {
                            dialog("choose character", SkinWindow.DEFAULT.name) {
                                table {
                                    textButton(
                                        CharacterType.WARRIOR.name,
                                        SkinTextButton.DEFAULT.name
                                    ) {
                                        onClick {
                                            chosenCharacterType = CharacterType.WARRIOR
                                            startSinglePlayerGame();
                                        }
                                    }
                                    row()
                                    textButton(
                                        CharacterType.ARCHER.name,
                                        SkinTextButton.DEFAULT.name
                                    ) {
                                        onClick {
                                            chosenCharacterType = CharacterType.ARCHER
                                            startSinglePlayerGame();
                                        }
                                    }
                                    row()
                                    textButton(
                                        CharacterType.PRIEST.name,
                                        SkinTextButton.DEFAULT.name
                                    ) {
                                        onClick {
                                            chosenCharacterType = CharacterType.PRIEST
                                            startSinglePlayerGame();
                                        }
                                    }
                                }
                            }
                        }
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
                                    textButton(CharacterType.WARRIOR.name, SkinTextButton.DEFAULT.name) {
                                        onClick {
                                            chosenCharacterType = CharacterType.WARRIOR
                                        }
                                    }
                                    row()
                                    textButton(CharacterType.ARCHER.name, SkinTextButton.DEFAULT.name) {
                                        onClick {
                                            chosenCharacterType = CharacterType.ARCHER
                                        }
                                    }
                                    row()
                                    textButton(CharacterType.PRIEST.name, SkinTextButton.DEFAULT.name) {
                                        onClick {
                                            chosenCharacterType = CharacterType.PRIEST
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
        if (assets.progress.isFinished && game.containsScreen<LobbyScreen>() &&
                ::chosenCharacterType.isInitialized && isValidLobbyID()) {
            changeToLobbyScreen()
        }

        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun isValidLobbyID(): Boolean {
        if (this.lobbyID.isBlank()) {
            return false;
        }

        if (lobbyID.matches(Regex("([A-z]){5}")) && lobbyID.length == 5) {
            return true;
        }

        return false;
    }

    private fun startSinglePlayerGame() {
        game.addScreen(LoadingScreen(game, socket, "", chosenCharacterType))
        game.removeScreen<MainScreen>()
        dispose()
        game.setScreen<LoadingScreen>()
    }

    private fun connectAndSetupSocket() {
        socket = IO.socket("http://localhost:9999")
        socket.connect()
        SocketOn.lobbyCreated(socket, callback = { lobbyID -> addLobbyScreen(lobbyID) });
        SocketOn.invalidLobbyID(socket, invalidLobbyID = { invalidLobbyID() });
        SocketOn.characterTaken(socket, characterTaken = { characterTaken() });
        SocketOn.joinLobbySuccessful(socket, callback = { addLobbyScreen() });

    }

    private fun createLobby() {
        if (chosenCharacterType == null) {
            return;
        }
        SocketEmit.createLobby(socket, chosenCharacterType!!.name);

    }

    private fun addLobbyScreen(lobbyID: String? = null) {
        if (lobbyID != null) {
            this.lobbyID = lobbyID;
        }

        if (game.containsScreen<LobbyScreen>()) {
            return
        }
        Gdx.app.log("Lobby", "adding screen with lobbyID$this.lobbyID")
        game.addScreen(LobbyScreen(game, this.lobbyID, socket, chosenCharacterType))
        Gdx.app.log("Lobby", "" + game.containsScreen<LobbyScreen>())
    }

    private fun changeToLobbyScreen() {
        game.removeScreen<MainScreen>()
        dispose()
        game.setScreen<LobbyScreen>()
    }

    private fun joinLobbyIfValid() {
        Gdx.app.log("Socket", "attempting to join lobby " + lobbyID + " with character " + chosenCharacterType?.name)
        val data = JSONObject();
        data.put("lobbyID", lobbyID);
        data.put("chosenCharacter", chosenCharacterType?.name);
        Gdx.app.log("Data", data.toString());
        SocketEmit.joinLobby(socket, data);
    }

    private fun invalidLobbyID() {
        invalidLobbyLabel.setText("invalid lobby ID")
        invalidLobbyLabel.color.a = 1f
    }

    private fun characterTaken() {
        invalidLobbyLabel.setText("character already taken")
        invalidLobbyLabel.color.a = 1f
    }
}
