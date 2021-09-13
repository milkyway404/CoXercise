package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.p4pProject.gameTutorial.*
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.socket.emit.SocketEmit
import com.p4pProject.gameTutorial.socket.on.SocketOn
import com.p4pProject.gameTutorial.ui.SkinImage
import com.p4pProject.gameTutorial.ui.SkinImageButton
import com.p4pProject.gameTutorial.ui.SkinTextField
import io.socket.client.Socket
import ktx.actors.onClick
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import ktx.scene2d.*
import kotlin.math.min

enum class CharacterType {
    WARRIOR {
        override val normalDamage: Int
            get() = 1
    }, ARCHER {
        override val normalDamage: Int
            get() = 2
    }, PRIEST {
        override val normalDamage: Int
            get() = 1
    };

    abstract val normalDamage: Int
}

private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: MyGameTutorial,
    private val engine: Engine = game.engine,
    private val socket: Socket,
    private val lobbyID: String,
    val chosenCharacterType: CharacterType,
): GameBaseScreen(game), GameEventListener {

    private lateinit var currentPlayer : Entity
    private lateinit var warrior: Entity
    private lateinit var archer: Entity
    private lateinit var priest: Entity
    private lateinit var boss : Entity
    private lateinit var hpBar: Image
    private lateinit var hpText: TextField
    private lateinit var mpBar: Image
    private lateinit var mpText: TextField
    private lateinit var bossHpBar: Image

    private var playerDead = false
    private var warriorDead = false
    private var archerDead = false
    private var priestDead = false
    private var gameOver = false

    private fun movePlayer(characterType: String, x: Float, y: Float) {
        val playerToMove = when (characterType) {
            CharacterType.WARRIOR.name -> warrior
            CharacterType.ARCHER.name -> archer
            CharacterType.PRIEST.name -> priest
            else -> return
        }

        val transform = playerToMove[TransformComponent.mapper]
        require(transform != null)

        transform.position.x = x
        transform.position.y = y
    }

    private fun spawnPlayers (){
         warrior = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,3f,-1f)
                setSize(20f * UNIT_SCALE, 20f * UNIT_SCALE)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent> {
                setAsWarrior()
            }
            with<FacingComponent>()
            with<WarriorAnimationComponent>()
        }

        archer = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,3f,-1f)
                setSize(20f * UNIT_SCALE, 20f * UNIT_SCALE)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>{
                setAsArcher()

            }
            with<FacingComponent>()
            with<ArcherAnimationComponent>()
        }

        priest = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,3f,-1f)
                setSize(20f * UNIT_SCALE, 20f * UNIT_SCALE)
            }
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent> {
                setAsPriest()

            }
            with<FacingComponent>()
            with<PriestAnimationComponent>()
        }


        currentPlayer = when (chosenCharacterType) {
            CharacterType.WARRIOR -> warrior
            CharacterType.ARCHER -> archer
            CharacterType.PRIEST -> priest
        }
    }

    private fun updateHp(hp: Float, maxHp: Float) {
        hpBar.scaleX = MathUtils.clamp(hp / maxHp, 0f, 1f)
        hpText.text = hp.toInt().toString()
    }

    private fun updateMp(mp: Float, maxMp: Float) {
        mpBar.scaleX = MathUtils.clamp(mp / maxMp, 0f, 1f)
        mpText.text = mp.toInt().toString()
    }

    private fun updatePlayerHpMp() {
        val playerComp = currentPlayer[PlayerComponent.mapper]!!
        updateHp(playerComp.hp.toFloat(), playerComp.maxHp.toFloat())
        updateMp(playerComp.mp.toFloat(), playerComp.maxMp.toFloat())
    }

    private fun updateBossHp() {
        val bossComp = boss[BossComponent.mapper]!!
        val bossTrans = boss[TransformComponent.mapper]!!
        bossHpBar.scaleX = MathUtils.clamp(bossComp.hp.toFloat() / bossComp.maxHp.toFloat(), 0f, 1f)
        bossHpBar.setPosition(bossTrans.position.x, bossTrans.position.y)
    }

    private fun spawnBoss(){
        boss = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,5f,-1f)
                setSize(50f * UNIT_SCALE, 50f * UNIT_SCALE)
            }
            with<BossAnimationComponent>()
            with<MoveComponent>()
            with<GraphicComponent>()
            with<BossComponent>()
            with<FacingComponent>()
            with<PlayerInfoComponent>{
                setPlayerInfo(warrior, archer, priest)
            }
        }
    }

    override fun show() {
        LOG.debug{ "Game screen is shown" }
        LOG.debug { "${preferences["highscore", 0f]}" }
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)
        gameEventManager.addListener(GameEvent.PlayerHit::class, this)
        gameEventManager.addListener(GameEvent.CollectPowerUp::class, this)
        gameEventManager.addListener(GameEvent.PlayerStep::class, this)
        gameEventManager.addListener(GameEvent.UpdateMp::class, this)
        //audioService.play(MusicAsset.GAME)
        spawnPlayers ()
        spawnBoss()

        val background = engine.entity{
            with<TransformComponent>()
            with<GraphicComponent> {
                isBackground()
            }
        }
        setupUI()
        setupSockets()
    }

    private fun setupSockets() {
        SocketOn.updatePlayersMove(socket,
                callback = { characterType, x, y -> movePlayer(characterType, x, y)})
        SocketOn.playerAttack(socket,
            callback = {characterType ->
                when (characterType) {
                    CharacterType.WARRIOR.name -> {
                        gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                            this.damage = CharacterType.WARRIOR.normalDamage
                            this.player = warrior
                        })
                    }
                    CharacterType.ARCHER.name -> {
                        val facing = archer[FacingComponent.mapper]
                        require(facing != null)
                        gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                            this.damage = CharacterType.ARCHER.normalDamage
                            this.player = archer
                            this.facing = facing.direction
                        })
                    }
                    CharacterType.PRIEST.name -> {
                        gameEventManager.dispatchEvent(GameEvent.PriestAttackEvent.apply {
                            this.damage = CharacterType.PRIEST.normalDamage
                            this.player = priest
                        })
                    }
                }

            })

        SocketOn.playerSpecialAttack(socket,
            callback = {characterType ->
                when (characterType) {
                    CharacterType.WARRIOR.name -> {
                        gameEventManager.dispatchEvent(GameEvent.WarriorSpecialAttackEvent.apply {
                            this.damage = 0
                            this.player = warrior
                        })
                    }
                    CharacterType.ARCHER.name -> {
                        val facing = archer[FacingComponent.mapper]
                        require(facing != null)
                        gameEventManager.dispatchEvent(GameEvent.ArcherSpecialAttackEvent.apply {
                            this.damage = 0
                            this.player = archer
                            this.facing = facing.direction
                        })
                    }
                    CharacterType.PRIEST.name -> {
                        gameEventManager.dispatchEvent(GameEvent.PriestSpecialAttackEvent.apply {
                            this.healing = 0
                            this.player = priest
                        })
                    }
                }

            })
    }

    override fun hide() {
        super.hide()
        gameEventManager.removeListener(GameEvent.PlayerDeath::class, this)
    }


    override fun render(delta: Float) {
        updateBossHp()
        engine.update(min(MAX_DELTA_TIME, delta))
        audioService.update()
        stage.run {
            viewport.apply()
            act()
            draw()
        }

        if (!playerDead) {
            updatePlayerHpMp()
            val transform = currentPlayer[TransformComponent.mapper]
            require(transform != null)

            if (lobbyID.isNotEmpty()) {
                SocketEmit.playerMove(socket, lobbyID, transform.position.x, transform.position.y)
            }
        }
    }

    private fun setupUI() {
        stage.actors {
            bossHpBar = image(SkinImage.HP_BAR.atlasKey) {
                color.a = 0.8f
                width = 100f
            }

            table {
                left().top()
                pad(3f)
                columnDefaults(0).width(50f)
                columnDefaults(0).height(8f)

                hpBar = image(SkinImage.HP_BAR.atlasKey) {
                    color.a = 0.8f
                }

                hpText = textField("-1", SkinTextField.DEFAULT.name)

                row()

                mpBar = image(SkinImage.MP_BAR.atlasKey) {
                    color.a = 0.8f
                }

                mpText = textField("1", SkinTextField.DEFAULT.name)

                setFillParent(true)
                pack()
            }

            table {
                right().bottom()
                pad(10f)
                when (chosenCharacterType) {
                    CharacterType.WARRIOR -> {
                        imageButton(SkinImageButton.WARRIOR_SPECIAL.name) {
                            color.a = 1.0f
                            onClick {
                                //TODO need to add mp logic here as event does not work correctly
                                SocketEmit.playerSpecialAttack(
                                    socket,
                                    lobbyID,
                                    CharacterType.WARRIOR.name
                                )
                                gameEventManager.dispatchEvent(GameEvent.WarriorSpecialAttackEvent.apply {
                                    this.damage = 0
                                    this.player = currentPlayer
                                })
                            }
                        }
                        row()
                        imageButton(SkinImageButton.WARRIOR_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                SocketEmit.playerAttack(socket, lobbyID, CharacterType.WARRIOR.name)
                                gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                                    this.damage = CharacterType.WARRIOR.normalDamage
                                    this.player = currentPlayer
                                })
                            }
                        }
                    }
                    CharacterType.ARCHER -> {
                        //TODO: add column default and big good image here for the buttons
                        imageButton(SkinImageButton.ARCHER_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                SocketEmit.playerSpecialAttack(
                                    socket,
                                    lobbyID,
                                    CharacterType.ARCHER.name
                                )
                                gameEventManager.dispatchEvent(GameEvent.ArcherSpecialAttackEvent.apply {
                                    val facing = currentPlayer[FacingComponent.mapper]
                                    require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }

                                    this.facing = facing.direction
                                    this.damage = 0
                                    this.player = currentPlayer
                                })
                            }
                        }
                        row()
                        imageButton(SkinImageButton.ARCHER_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                SocketEmit.playerAttack(socket, lobbyID, CharacterType.ARCHER.name)
                                gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                                    val facing = currentPlayer[FacingComponent.mapper]
                                    require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                    this.facing = facing.direction
                                    this.damage = CharacterType.ARCHER.normalDamage
                                    this.player = currentPlayer
                                })
                            }
                        }
                    }
                    CharacterType.PRIEST -> {
                        imageButton(SkinImageButton.PRIEST_SPECIAL.name) {
                            color.a = 1.0f
                            onClick {
                                SocketEmit.playerSpecialAttack(
                                    socket,
                                    lobbyID,
                                    CharacterType.PRIEST.name
                                )
                                gameEventManager.dispatchEvent(GameEvent.PriestSpecialAttackEvent.apply {
                                    val facing = currentPlayer[FacingComponent.mapper]
                                    require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                    this.healing = 25
                                    this.player = currentPlayer
                                })
                            }
                        }
                        row()
                        imageButton(SkinImageButton.PRIEST_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                SocketEmit.playerAttack(socket, lobbyID, CharacterType.PRIEST.name)
                                gameEventManager.dispatchEvent(GameEvent.PriestAttackEvent.apply {
                                    val facing = currentPlayer[FacingComponent.mapper]
                                    require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                    this.damage = CharacterType.PRIEST.normalDamage
                                    this.player = currentPlayer
                                })
                            }
                        }
                    }
                }

                setFillParent(true)
                pack()
            }
        }
        updatePlayerHpMp()
        // allows you to see the borders of components on screen
        stage.isDebugAll = true
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PlayerDeath) {
            when (event.characterType) {
                CharacterType.WARRIOR -> {
                    if (currentPlayer == warrior) playerDead = true
                    warriorDead = true
                }
                CharacterType.ARCHER -> {
                    if (currentPlayer == archer) playerDead = true
                    archerDead = true
                }
                CharacterType.PRIEST -> {
                    if (currentPlayer == priest) playerDead = true
                    priestDead = true
                }
            }
        }
        else if (event is GameEvent.PlayerHit) {
            updateHp(event.hp.toFloat(), event.maxHp.toFloat())
        }
        else if (event is GameEvent.CollectPowerUp) {
            val mp = event.player[PlayerComponent.mapper]?.mp?.toFloat()
            val maxMp = event.player[PlayerComponent.mapper]?.maxMp?.toFloat()
            LOG.debug{ "Collected powerup, mp=$mp, maxMP=$maxMp" }
            if (mp != null && maxMp != null) {
                updateMp(mp, maxMp)
            }
        }
        else if (event is GameEvent.UpdateMp) {
            val mp = event.player.mp.toFloat()
            val maxMp = event.player.maxMp.toFloat()
            updateMp(mp, maxMp)
        }
        else if (event is GameEvent.PlayerStep) {
            val mp = event.player.mp.toFloat()
            val maxMp = event.player.maxMp.toFloat()
            updateMp(mp, maxMp)
        }
    }
}