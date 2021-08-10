package com.p4pProject.gameTutorial.screen

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Game
import com.p4pProject.gameTutorial.*
import com.p4pProject.gameTutorial.ecs.asset.MusicAsset
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventListener
import com.p4pProject.gameTutorial.ui.SkinImageButton
import ktx.actors.onClick
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import ktx.scene2d.*
import java.time.LocalDateTime
import kotlin.math.min


private val LOG = logger<MyGameTutorial>()
private const val MAX_DELTA_TIME = 1/20f

class GameScreen(
    game: MyGameTutorial,
    private val engine: Engine = game.engine
): GameTutorialScreen(game), GameEventListener {

    private lateinit var playerr : Entity

    private fun spawnPlayer (){
         playerr = engine.entity{
            with<TransformComponent>{
                setInitialPosition(9f,3f,-1f)
                setSize(20f * UNIT_SCALE, 20f * UNIT_SCALE)
            }
            with<PlayerAnimationComponent>()
            with<MoveComponent>()
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }


        // The added fire
        /*engine.entity {
            with<TransformComponent>()
            with<AttachComponent> {
                entity = player
                offset.set(1f * UNIT_SCALE, -6f * UNIT_SCALE)
            }
            with<GraphicComponent>()
            with<AnimationComponent> {
                type = AnimationType.FIRE
            }
        }*/
    }

    override fun show() {
        LOG.debug{ "Game screen is shown" }
        LOG.debug { "${preferences["highscore", 0f]}" }
        gameEventManager.addListener(GameEvent.PlayerDeath::class, this)

        audioService.play(MusicAsset.GAME)
        spawnPlayer ()

        val background = engine.entity{
            with<TransformComponent>()
            with<GraphicComponent> {
                isBackground()
            }
        }
        setupUI()
    }

    override fun hide() {
        super.hide()
        gameEventManager.removeListener(GameEvent.PlayerDeath::class, this)
    }


    override fun render(delta: Float) {
        engine.update(min(MAX_DELTA_TIME, delta))
        audioService.update()
        stage.run {
            viewport.apply()
            act()
            draw()
        }
    }

    private fun setupUI() {
        stage.actors {
            table {
                right().bottom()
                pad(5f)
                imageButton(SkinImageButton.WARRIOR_ATTACK.name) {
                    color.a = 1.0f
                    onClick {
                        gameEventManager.dispatchEvent(GameEvent.PlayerAttack.apply {
                            this.damage = 50f
                            this.player = playerr
                        })
                    }
                }
                setFillParent(true)
                pack()
            }
        }
        // allows you to see the borders of components on screen
        stage.isDebugAll = true

        // TODO implement actual boss logic
        gameEventManager.dispatchEvent(GameEvent.BossAttack.apply {
            this.damage = 20
            this.startX = 0
            this.endX = 100
            this.startY = 0
            this.endY = 100
            this.startTime = LocalDateTime.now()
            this.duration = 20
        })
    }

    override fun onEvent(event: GameEvent) {
        when (event){
            is GameEvent.PlayerDeath -> {
                LOG.debug { "Player died with a distance of ${event.distance}" }
                preferences.flush {
                    this["highscore"] = event.distance
                }
                spawnPlayer()
            }
        }
    }
}