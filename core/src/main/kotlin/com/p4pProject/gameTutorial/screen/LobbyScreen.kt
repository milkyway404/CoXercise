package com.p4pProject.gameTutorial.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import com.p4pProject.gameTutorial.MyGameTutorial
import com.p4pProject.gameTutorial.socket.emit.SocketEmit
import com.p4pProject.gameTutorial.socket.on.SocketOn
import com.p4pProject.gameTutorial.ui.SkinLabel
import com.p4pProject.gameTutorial.ui.SkinTextButton
import com.p4pProject.gameTutorial.ui.SkinTextField
import io.socket.client.Socket
import ktx.actors.onClick
import ktx.scene2d.*

const val NUM_PLAYERS = 3;

class LobbyScreen (
    game: MyGameTutorial,
    private val lobbyID: String,
    private val socket: Socket,
    private val chosenCharacterType: CharacterType ) : GameBaseScreen(game) {

    data class Player (val socketID: String, val characterType: String);

    private var playersList: List<Player> = ArrayList();
    private var playersTextFieldList: ArrayList<TextField> = ArrayList();
    private lateinit var startButton:TextButton;
    private var isGameStarted: Boolean = false;

    override fun show() {
        Gdx.app.log("Lobby", lobbyID)
        setupUI()
        setupSockets()
    }

    override fun render(delta: Float) {
        if (isGameStarted) {
            game.addScreen(LoadingScreen(game, socket, lobbyID, chosenCharacterType))
            game.removeScreen<LobbyScreen>()
            dispose()
            game.setScreen<LoadingScreen>()
        }

        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun setupSockets() {
        SocketOn.updatePlayers(socket, callback = { playersList -> updatePlayers(playersList)})
        SocketOn.startGame(socket, callback = { startGame() });

        SocketEmit.getLobbyPlayers(socket, lobbyID);
    }

    private fun updatePlayers(playersList: List<Player>) {
        this.playersList = playersList;
        for (i in 0 until NUM_PLAYERS) {
            if (playersList.size > i) {
                playersTextFieldList[i].text = playersList[i].characterType;
            }
        }

        if (playersTextFieldList.size == NUM_PLAYERS) {
            startButton.color.a = 1f;
            startButton.isDisabled = false;
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                label(lobbyID, SkinLabel.LARGE.name) {
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
                startButton = textButton("Start", SkinTextButton.DEFAULT.name) {
                    color.a = 0f;
                    isDisabled = true;
                    onClick {
                        SocketEmit.startGame(socket, lobbyID);
                        startGame()
                    }
                }
            }
            stage.isDebugAll = true
        }
    }

    private fun startGame() {
        isGameStarted = true;
    }
}