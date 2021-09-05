package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
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
    WARRIOR, ARCHER, PRIEST, BOSS
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
            CharacterType.BOSS -> boss
        }
    }

    private fun updatePlayerHpMp() {

        if(chosenCharacterType != CharacterType.BOSS){
            val playerComp = currentPlayer[PlayerComponent.mapper]!!
            updateHp(playerComp.hp.toFloat(), playerComp.maxHp.toFloat())
            updateMp(playerComp.mp.toFloat(), playerComp.maxMp.toFloat())
        }else{
            val bossComp = boss[BossComponent.mapper]!!
            updateHp(bossComp.hp.toFloat(), bossComp.maxHp.toFloat())
            updateMp(0.toFloat(), 0.toFloat())
        }


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
        spawnBoss()
        spawnPlayers ()

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
                callback = { characterType, x, y -> movePlayer(characterType, x, y)});
    }

    override fun hide() {
        super.hide()
        gameEventManager.removeListener(GameEvent.PlayerDeath::class, this)
    }


    override fun render(delta: Float) {
        updatePlayerHpMp()
        engine.update(min(MAX_DELTA_TIME, delta))
        audioService.update()
        stage.run {
            viewport.apply()
            act()
            draw()
        }

        val transform = currentPlayer[TransformComponent.mapper]
        require(transform != null)

        if (lobbyID.isNotEmpty()) {
            SocketEmit.playerMove(socket, lobbyID, transform.position.x, transform.position.y)
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                left().top();
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

                mpText = textField("1",SkinTextField.DEFAULT.name)

                setFillParent(true)
                pack()
            }

            table {
                right().bottom()
                pad(10f)
                if (chosenCharacterType == CharacterType.WARRIOR) {
                    imageButton(SkinImageButton.WARRIOR_SPECIAL.name) {
                        color.a = 1.0f
                        onClick {
                            //TODO need to add mp logic here as event does not work correctly
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
                            gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                                this.damage = 0
                                this.player = currentPlayer
                            })
                        }
                    }
                } else if (chosenCharacterType == CharacterType.ARCHER) {
                    //TODO: add column default and big good image here for the buttons
                    imageButton(SkinImageButton.ARCHER_ATTACK.name) {
                        color.a = 1.0f
                        onClick {

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

                            gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                                val facing = currentPlayer[FacingComponent.mapper]
                                require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }

                                this.facing = facing.direction
                                this.damage = 0
                                this.player = currentPlayer
                            })
                        }
                    }
                }

                else if (chosenCharacterType == CharacterType.PRIEST) {
                    imageButton(SkinImageButton.PRIEST_SPECIAL.name) {
                        color.a = 1.0f
                        onClick {

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

                            gameEventManager.dispatchEvent(GameEvent.PriestAttackEvent.apply {
                                val facing = currentPlayer[FacingComponent.mapper]
                                require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                this.damage = 0
                                this.player = currentPlayer
                            })
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

        // TODO implement actual boss logic
//        gameEventManager.dispatchEvent(GameEvent.BossAttack.apply {
//            this.damage = 1
//            this.startX = 0
//            this.endX = 5
//            this.startY = 0
//            this.endY = 5
//            this.startTime = LocalDateTime.now()
//            this.duration = 2000
//        })
    }

    private fun updateHp(hp: Float, maxHp: Float) {
        hpBar.scaleX = MathUtils.clamp(hp / maxHp, 0f, 1f)
        hpText.text = hp.toInt().toString()
    }

    private fun updateMp(mp: Float, maxMp: Float) {
        mpBar.scaleX = MathUtils.clamp(mp / maxMp, 0f, 1f)
        mpText.text = mp.toInt().toString()
    }

    override fun onEvent(event: GameEvent) {
        when (event){
            is GameEvent.PlayerDeath -> {
                LOG.debug { "Player died with a distance of ${event.distance}" }
                preferences.flush {
                    this["highscore"] = event.distance
                }
                spawnPlayers()
            }
            is GameEvent.PlayerHit -> {
                updateHp(event.hp.toFloat(), event.maxHp.toFloat())
            }
            is GameEvent.CollectPowerUp -> {
                val mp = event.player[PlayerComponent.mapper]?.mp?.toFloat()
                val maxMp = event.player[PlayerComponent.mapper]?.maxMp?.toFloat()
                LOG.debug{ "Collected powerup, mp=$mp, maxMP=$maxMp" }
                if (mp != null && maxMp != null) {
                    updateMp(mp, maxMp)
                }
            }
            is GameEvent.UpdateMp ->{
                val mp = event.player.mp.toFloat()
                val maxMp = event.player.maxMp.toFloat()
                updateMp(mp, maxMp)
            }
            is GameEvent.PlayerStep -> {
                val mp = event.player.mp.toFloat()
                val maxMp = event.player.maxMp.toFloat()
                updateMp(mp, maxMp)
            }
        }
    }
}