package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
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
import ktx.ashley.addComponent
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import ktx.preferences.get
import ktx.scene2d.*
import kotlin.math.min



enum class CharacterType {
    WARRIOR, SLINGER, NECROMANCER
}

private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: MyGameTutorial,
    private val engine: Engine = game.engine,
    private val socket: Socket?,
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
    private lateinit var warriorSpecialAttackBtn: ImageButton
    private lateinit var archerSpecialAttackBtn: ImageButton
    private lateinit var priestSpecialAttackBtn: ImageButton
    private lateinit var shapeRenderer: ShapeRenderer



    var playerDead = false
    private var warriorDead = false
    private var archerDead = false
    private var priestDead = false

    private fun movePlayer(characterType: String, x: Float, y: Float) {
        val playerToMove = when (characterType) {
            CharacterType.WARRIOR.name -> warrior
            CharacterType.SLINGER.name -> archer
            CharacterType.NECROMANCER.name -> priest
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
                setInitialPosition(6f,3f,-1f)
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
                setInitialPosition(3f,3f,-1f)
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
            CharacterType.SLINGER -> archer
            CharacterType.NECROMANCER -> priest
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

        if (playerComp.mp >= playerComp.specialAttackMpCost) {
            when (playerComp.characterType) {
                CharacterType.WARRIOR -> {
                    if (this::warriorSpecialAttackBtn.isInitialized) {
                        warriorSpecialAttackBtn.color.a = 1f
                        warriorSpecialAttackBtn.isDisabled = false
                    }
                }
                CharacterType.SLINGER -> {
                    if (this::archerSpecialAttackBtn.isInitialized) {
                        archerSpecialAttackBtn.color.a = 1f
                        archerSpecialAttackBtn.isDisabled = false
                    }
                }
                CharacterType.NECROMANCER -> {
                    if (this::priestSpecialAttackBtn.isInitialized) {
                        priestSpecialAttackBtn.color.a = 1f
                        priestSpecialAttackBtn.isDisabled = false
                    }
                }
            }
        } else {
            when (playerComp.characterType) {
                CharacterType.WARRIOR -> {
                    if (this::warriorSpecialAttackBtn.isInitialized) {
                        warriorSpecialAttackBtn.color.a = 0.5f
                        warriorSpecialAttackBtn.isDisabled = true
                    }
                }
                CharacterType.SLINGER -> {
                    if (this::archerSpecialAttackBtn.isInitialized) {
                        archerSpecialAttackBtn.color.a = 0.5f
                        archerSpecialAttackBtn.isDisabled = true
                    }
                }
                CharacterType.NECROMANCER -> {
                    if (this::priestSpecialAttackBtn.isInitialized) {
                        priestSpecialAttackBtn.color.a = 0.5f
                        priestSpecialAttackBtn.isDisabled = true
                    }
                }
            }

        }
    }

    private fun updateBossHp() {
        val bossComp = boss[BossComponent.mapper]!!
        bossHpBar.rotation = 180f
        bossHpBar.scaleX = MathUtils.clamp(bossComp.hp.toFloat() / bossComp.maxHp.toFloat(), 0f, 1f)
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
        gameEventManager.addListener(GameEvent.BossDead::class, this)
        //audioService.play(MusicAsset.GAME)
        spawnPlayers()
        spawnBoss()
        addBossInfoToPlayers()

        val background = engine.entity{
            with<TransformComponent>()
            with<GraphicComponent> {
                isBackground()
            }
        }
        setupUI()
        setupSockets()

        shapeRenderer = ShapeRenderer();
    }

    private fun setupSockets() {
        if (socket == null) {
            return
        }

        SocketOn.updatePlayersMove(socket,
                callback = { characterType, x, y -> movePlayer(characterType, x, y)})
        SocketOn.playerAttack(socket,
            callback = {characterType ->
                when (characterType) {
                    CharacterType.WARRIOR.name -> {
                        gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                            this.player = warrior
                        })
                    }
                    CharacterType.SLINGER.name -> {
                        val facing = archer[FacingComponent.mapper]
                        require(facing != null)
                        gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                            this.player = archer
                        })
                    }
                    CharacterType.NECROMANCER.name -> {
                        gameEventManager.dispatchEvent(GameEvent.PriestAttackEvent.apply {
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
                            this.player = warrior
                        })
                    }
                    CharacterType.SLINGER.name -> {
                        val facing = archer[FacingComponent.mapper]
                        require(facing != null)
                        gameEventManager.dispatchEvent(GameEvent.ArcherSpecialAttackEvent.apply {
                            this.player = archer
                        })
                    }
                    CharacterType.NECROMANCER.name -> {
                        gameEventManager.dispatchEvent(GameEvent.PriestSpecialAttackEvent.apply {
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
            val transform = boss[TransformComponent.mapper]
            require(transform != null)

            if (lobbyID.isNotEmpty() && socket != null) {
                SocketEmit.playerMove(socket, lobbyID, transform.position.x, transform.position.y)
            }
        }

        //renderPlayersAndBoss()
    }

    private fun renderPlayersAndBoss() {
        shapeRenderer.projectionMatrix = gameViewport.camera.combined

        if (!warriorDead) {
            val warriorTrans = warrior[TransformComponent.mapper]!!
            shapeRenderer.color = Color.RED
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.rect(warriorTrans.position.x, warriorTrans.position.y, warriorTrans.size.x, warriorTrans.size.y)
            shapeRenderer.end()
        }

        if (!archerDead) {
            val archerTrans = archer[TransformComponent.mapper]!!
            shapeRenderer.color = Color.BLUE
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.rect(archerTrans.position.x, archerTrans.position.y, archerTrans.size.x, archerTrans.size.y)
            shapeRenderer.end()
        }

        if (!priestDead) {
            val priestTrans = priest[TransformComponent.mapper]!!
            shapeRenderer.color = Color.LIME
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.rect(priestTrans.position.x, priestTrans.position.y, priestTrans.size.x, priestTrans.size.y)
            shapeRenderer.end()
        }

        val bossTrans = boss[TransformComponent.mapper]!!
        shapeRenderer.color = Color.BLACK
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.rect(bossTrans.position.x, bossTrans.position.y, bossTrans.size.x, bossTrans.size.y)
        shapeRenderer.end()
    }

    private fun setupUI() {
        stage.actors {

            table {
                right().top()
                padRight(-97f)
                padTop(-5f)
                columnDefaults(0).width(100f)
                columnDefaults(0).height(8f)

                bossHpBar = image(SkinImage.HP_BAR.atlasKey) {
                    color.a = 0.8f
                    rotation = 180f
                }

                setFillParent(true)
                pack()
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
                        warriorSpecialAttackBtn = imageButton(SkinImageButton.WARRIOR_SPECIAL.name) {
                            color.a = 1.0f
                            onClick {
                                if (!isDisabled && !warriorDead) {
                                    val playerComp = currentPlayer[PlayerComponent.mapper]!!
                                    playerComp.mp -= playerComp.specialAttackMpCost
                                    emitPlayerSpecialAttack(CharacterType.WARRIOR)
                                    if(!warriorDead){
                                        gameEventManager.dispatchEvent(GameEvent.WarriorSpecialAttackEvent.apply {
                                            this.player = currentPlayer
                                        })
                                    }
                                }
                            }
                            isDisabled = true
                        }
                        row()
                        imageButton(SkinImageButton.WARRIOR_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                if(!warriorDead){
                                    emitPlayerAttack(CharacterType.WARRIOR)
                                    gameEventManager.dispatchEvent(GameEvent.WarriorAttackEvent.apply {
                                        this.player = currentPlayer
                                    })
                                }
                            }
                        }
                    }
                    CharacterType.SLINGER -> {
                        //TODO: add column default and big good image here for the buttons
                        archerSpecialAttackBtn = imageButton(SkinImageButton.ARCHER_SPECIAL.name) {
                            color.a = 1.0f
                            onClick {
                                if (!isDisabled && !archerDead) {
                                    val playerComp = currentPlayer[PlayerComponent.mapper]!!
                                    playerComp.mp -= playerComp.specialAttackMpCost
                                    emitPlayerSpecialAttack(CharacterType.SLINGER)
                                    gameEventManager.dispatchEvent(GameEvent.ArcherSpecialAttackEvent.apply {
                                        val facing = currentPlayer[FacingComponent.mapper]
                                        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                        this.player = currentPlayer
                                    })
                                }
                            }
                            isDisabled = true
                        }
                        row()
                        imageButton(SkinImageButton.ARCHER_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                if(!archerDead){
                                    emitPlayerAttack(CharacterType.SLINGER)
                                    gameEventManager.dispatchEvent(GameEvent.ArcherAttackEvent.apply {
                                        val facing = currentPlayer[FacingComponent.mapper]
                                        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }

                                        this.player = currentPlayer
                                    })
                                }
                            }
                        }
                    }
                    CharacterType.NECROMANCER -> {
                        priestSpecialAttackBtn = imageButton(SkinImageButton.PRIEST_SPECIAL.name) {
                            color.a = 1.0f
                            onClick {
                                if (!isDisabled && !priestDead) {
                                    val playerComp = currentPlayer[PlayerComponent.mapper]!!
                                    playerComp.mp -= playerComp.specialAttackMpCost
                                    emitPlayerSpecialAttack(CharacterType.NECROMANCER)

                                    if(!priestDead){
                                        gameEventManager.dispatchEvent(GameEvent.PriestSpecialAttackEvent.apply {
                                            val facing = currentPlayer[FacingComponent.mapper]
                                            require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                            this.player = currentPlayer
                                        })
                                    }
                                }
                            }
                            isDisabled = true
                        }
                        row()
                        imageButton(SkinImageButton.PRIEST_ATTACK.name) {
                            color.a = 1.0f
                            onClick {
                                if(!priestDead){
                                    emitPlayerAttack(CharacterType.NECROMANCER)
                                    gameEventManager.dispatchEvent(GameEvent.PriestAttackEvent.apply {
                                        val facing = currentPlayer[FacingComponent.mapper]
                                        require(facing != null) { "Entity |entity| must have a FacingComponent. entity=$currentPlayer" }
                                        this.player = currentPlayer
                                    })
                                }
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
        stage.isDebugAll = false
    }

    override fun onEvent(event: GameEvent) {
        if (event is GameEvent.PlayerDeath) {
            when (event.characterType) {
                CharacterType.WARRIOR -> {
                    if (currentPlayer == warrior) playerDead = true
                    warriorDead = true
                }
                CharacterType.SLINGER -> {
                    if (currentPlayer == archer) playerDead = true
                    archerDead = true
                }
                CharacterType.NECROMANCER -> {
                    if (currentPlayer == priest) playerDead = true
                    priestDead = true
                }
            }
            if(warriorDead && archerDead && priestDead){
                game.removeScreen<GameScreen>()
                game.addScreen(PlayerLoseScreen(MyGameTutorial()))
                game.setScreen<PlayerLoseScreen>()
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

        else if(event is GameEvent.BossDead){
            game.removeScreen<GameScreen>()
            game.addScreen(PlayerWinScreen(MyGameTutorial()))
            game.setScreen<PlayerWinScreen>()
        }
    }

    private fun emitPlayerAttack(characterType: CharacterType) {
        if (socket != null) {
            SocketEmit.playerAttack(socket, lobbyID, characterType.name)
        }
    }

    private fun emitPlayerSpecialAttack(characterType: CharacterType) {
        if (socket != null) {
            SocketEmit.playerSpecialAttack(socket, lobbyID, characterType.name)
        }
    }

    private fun addBossInfoToPlayers() {
        warrior.addComponent<BossInfoComponent>(engine) {
            boss = this@GameScreen.boss
        }
        archer.addComponent<BossInfoComponent>(engine) {
            boss = this@GameScreen.boss
        }
        priest.addComponent<BossInfoComponent>(engine) {
            boss = this@GameScreen.boss
        }
    }
}